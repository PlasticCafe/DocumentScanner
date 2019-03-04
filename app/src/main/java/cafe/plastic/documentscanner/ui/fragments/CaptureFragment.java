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
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.ChangeBounds;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import androidx.transition.TransitionManager;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.databinding.CaptureFragmentBinding;
import cafe.plastic.documentscanner.vision.ObjectTracker;
import cafe.plastic.pagedetect.PageDetector;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.selector.FlashSelectorsKt;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CaptureFragment extends Fragment {
    private static final long MAX_LOCK_TIME = 2300;
    private CaptureViewModel viewModel;
    private Fotoapparat fotoapparat;
    private CaptureFragmentBinding binding;
    private final ObjectTracker pageTracker = new ObjectTracker();
    private final CompositeDisposable observers = new CompositeDisposable();
    private final PublishProcessor<Bitmap> captureEvents = PublishProcessor.create();
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
        viewModel = ViewModelProviders.of(requireActivity()).get(CaptureViewModel.class);
        viewModel.flashMode.observe(this, f -> configureCamera());
        viewModel.captureMode.observe(this, o -> configureCamera());
        binding.setHandlers(new Handlers());
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        initializeCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        initializeCamera();
        fotoapparat.start();
        configureObservers();
        configureCamera();
        animate(R.layout.capture_fragment_capturing_off, 0, new LinearInterpolator());
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
        fotoapparat.stop();
        observers.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        pageTracker.close();
    }

    private void configureObservers() {

        observers.add(
                detectionEvents
                        .sample(200, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .timeInterval()
                        .scan(0L, (acc, current) -> {
                            if (current.value().state == PageDetector.State.LOCKED) {
                                acc += current.time();
                                if (acc > MAX_LOCK_TIME) {
                                    acc = MAX_LOCK_TIME;
                                }
                            } else {
                                if (acc < current.time())
                                    acc = 0L;
                                else
                                    acc -= current.time();
                            }
                            binding.featureOverlay.updateRegion(current.value());
                            binding.featureOverlay.updateLockTime((float) acc / MAX_LOCK_TIME);
                            viewModel.captureState.setValue(current.value().state);
                            return acc;
                        })
                        .filter(t -> t >= MAX_LOCK_TIME)
                        .withLatestFrom(detectionEvents.filter(r -> r.state == PageDetector.State.LOCKED), Pair::new)
                        .filter(t -> viewModel.captureMode.getValue() == CameraState.CaptureMode.AUTO)
                        .take(1)
                        .map(i -> {
                            animate(R.layout.capture_fragment_capturing_on, 120, new LinearInterpolator());
                            viewModel.captureState.setValue(PageDetector.State.CAPTURE);
                            return i;
                        })
                        .observeOn(Schedulers.computation())
                        .map(e -> {
                            Bitmap bitmap = fotoapparat.takePicture().toBitmap().await().bitmap;
                            return pageTracker.processPhoto(bitmap, new PageDetector.Region(e.second));
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(b -> {
                            captureEvents.onNext(b);
                        }));

        observers.add(
                captureEvents
                        .observeOn(Schedulers.io())
                        .map(b -> {
                            Timber.d("Current thread: " + Thread.currentThread().getName());
                            return viewModel.imageManager.storeTempBitmap(b);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> {
                            Timber.d("Image saved to: " + s);
                            Toast.makeText(getContext(), "Image saved to: " + s, Toast.LENGTH_SHORT).show();
                            CaptureFragmentDirections.ConfirmAction action = CaptureFragmentDirections.confirmAction(s);
                            NavHostFragment.findNavController(CaptureFragment.this).navigate(action);
                        }));
    }

    private void initializeCamera() {
        fotoapparat = Fotoapparat.with(requireActivity())
                .into(binding.cameraView
                )
                .focusView(binding.focusView)
                .previewScaleType(ScaleType.CenterCrop)
                .frameProcessor(pageTracker)
                .build();
    }

    private void configureCamera() {
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

