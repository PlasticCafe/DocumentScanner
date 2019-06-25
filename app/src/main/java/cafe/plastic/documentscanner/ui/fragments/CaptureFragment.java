package cafe.plastic.documentscanner.ui.fragments;

import android.Manifest;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProviders;

import android.animation.TimeInterpolator;


import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.ChangeBounds;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.transition.TransitionManager;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.databinding.CaptureFragmentBinding;
import cafe.plastic.documentscanner.util.TempImageManager;
import cafe.plastic.documentscanner.vision.ObjectTracker;
import cafe.plastic.pagedetect.PageDetector;
import cafe.plastic.pagedetect.PostProcess;
import cafe.plastic.pagedetect.Quad;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.selector.FlashSelectorsKt;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;
import timber.log.Timber;

public class CaptureFragment extends Fragment implements BackButtonPressed {
    private static final long MAX_LOCK_TIME = 2300;
    private CaptureViewModel viewModel;
    private Fotoapparat fotoapparat;
    private CaptureFragmentBinding binding;
    private final ObjectTracker pageTracker = new ObjectTracker();
    private final CompositeDisposable observers = new CompositeDisposable();
    private final Flowable<PageDetector.Region> detectionEvents = pageTracker.processedOutput();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = CaptureFragmentBinding.inflate(inflater, container, false);
        binding.setHandlers(new Handlers());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            if(TempImageManager.getInstance(getContext()).loadTempBitmap() != null) {
                openConfirmationFragment();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        initialize();
        initializeResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
        fotoapparat.stop();
        cancelObservers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        pageTracker.close();
    }

    private void initialize() {
        viewModel = ViewModelProviders.of(requireActivity()).get(CaptureViewModel.class);
        viewModel.flashMode.observe(this, f -> configureCamera());
        viewModel.captureMode.observe(this, o -> configureCamera());
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);
        //requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

    }

    private void initializeResume() {
        initializeCamera();
        configureObservers();
        hideLoadingUI();
        try {
            if(TempImageManager.getInstance(getContext()).loadTempBitmap() != null) {
                openConfirmationFragment();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openConfirmationFragment() {
        Timber.d("Opening confirmation fragment.");
        Navigation.findNavController(getView()).navigate(CaptureFragmentDirections.confirmAction());
    }

    private void initializeCamera() {
        Timber.d("Initializing camera");
        if(fotoapparat != null) fotoapparat.stop();
        fotoapparat = Fotoapparat.with(requireActivity())
                .into(binding.cameraView
                )
                .focusView(binding.focusView)
                .previewScaleType(ScaleType.CenterCrop)
                .frameProcessor(pageTracker)
                .build();
        fotoapparat.start();
        configureCamera();
    }

    private void configureCamera() {
        Timber.d("Configuring camera");
        CameraConfiguration.Builder builder = new CameraConfiguration.Builder();
        switch (viewModel.flashMode.getValue()) {
            case OFF:
                builder.flash(FlashSelectorsKt.off());
                break;
            case ON:
                builder.flash(FlashSelectorsKt.torch());
                break;
            default:
                builder.flash(FlashSelectorsKt.off());
        }
        fotoapparat.updateConfiguration(builder.build());
        fotoapparat.start();
    }

    private void configureObservers() {
        observers.add(
                configureDetectionObserver()
                        .subscribe(s -> {
                            openConfirmationFragment();
                        }));
    }

    private void cancelObservers() {
        observers.clear();
    }

    private Flowable<PostProcess.RenderConfiguration> configureDetectionObserver() {
        return detectionEvents
                .sample(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .timeInterval()
                .scan(0L, this::calculateLockTime)
                .filter(t -> t >= MAX_LOCK_TIME)
                .withLatestFrom(detectionEvents.filter(r -> r.state == PageDetector.State.LOCKED), Pair::new)
                .filter(t -> viewModel.captureMode.getValue() == CameraState.CaptureMode.AUTO)
                .take(1)
                .doOnNext((i) -> showCaptureUI())
                .observeOn(Schedulers.computation())
                .map(e -> capture(e.second))
                .observeOn(Schedulers.io())
                .doOnNext(viewModel.imageManager::storeTempImage)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext((s) -> {
                    Timber.d("Image saved to: %s", s);
                    Toast.makeText(getContext(), "Image saved to: " + s, Toast.LENGTH_SHORT).show();
                });
    }

    private PostProcess.RenderConfiguration capture(PageDetector.Region region) throws ExecutionException, InterruptedException {
        Bitmap bitmap = fotoapparat.takePicture().toBitmap().await().bitmap;
        PostProcess.RenderConfiguration.Builder builder = new PostProcess.RenderConfiguration.Builder(bitmap);
        Quad roi = region.roi.copy();
        roi.scale((float)bitmap.getWidth()/region.frameSize.getWidth());
        return builder.brightness(50.0f)
                .contrast(1.0f)
                .region(roi)
                .threshold(false)
                .rotation(region.rotation)
                .scale(0.30f)
                .build();
    }

    private void showCaptureUI() {
        Timber.d("Showing capture ui");
        animate(R.layout.capture_fragment_capturing_on, 120, new LinearInterpolator());
        viewModel.captureState.setValue(PageDetector.State.CAPTURE);
    }

    private void hideLoadingUI() {
        Timber.d("Hiding capture ui");
        animate(R.layout.capture_fragment_capturing_off, 120, new LinearInterpolator());
    }

    private Long calculateLockTime(Long accumulator, Timed<PageDetector.Region> currentTime) {
        if (currentTime.value().state == PageDetector.State.LOCKED) {
            accumulator += currentTime.time();
            if (accumulator > MAX_LOCK_TIME) {
                accumulator = MAX_LOCK_TIME;
            }
        } else {
            if (accumulator < currentTime.time())
                accumulator = 0L;
            else
                accumulator -= currentTime.time();
        }
        binding.featureOverlay.updateRegion(currentTime.value());
        binding.featureOverlay.updateLockTime((float) accumulator / MAX_LOCK_TIME);
        viewModel.captureState.setValue(currentTime.value().state);
        return accumulator;
    }




    private void animate(int targetLayout, int time, TimeInterpolator interpolator) {
        ConstraintSet target = new ConstraintSet();
        ChangeBounds transition = new ChangeBounds();
        target.clone(getActivity(), targetLayout);
        transition.setInterpolator(interpolator);
        transition.setDuration(time);
        TransitionManager.beginDelayedTransition(binding.constraintLayout, transition);
        target.applyTo(binding.constraintLayout);
    }

    @Override
    public void onSupportNavigateUp() {
        Timber.d("onSupportNavigateUp Capture Fragment");
        NavHostFragment.findNavController(this).popBackStack();
    }

    public class Handlers {

        public void onFlashButtonClicked(View view) {
            CameraState.FlashMode flash = viewModel.flashMode.getValue();
            if (flash == CameraState.FlashMode.ON) {
                flash = CameraState.FlashMode.OFF;
            } else {
                flash = CameraState.FlashMode.ON;
            }
            viewModel.flashMode.setValue(flash);
        }

        public void onCaptureModeButtonClicked(View view) {
            CameraState.CaptureMode outline = viewModel.captureMode.getValue();
            if (outline == CameraState.CaptureMode.AUTO) {
                outline = CameraState.CaptureMode.MANUAL;

            } else {
                outline = CameraState.CaptureMode.AUTO;
            }
            viewModel.captureMode.setValue(outline);
        }
    }
}

