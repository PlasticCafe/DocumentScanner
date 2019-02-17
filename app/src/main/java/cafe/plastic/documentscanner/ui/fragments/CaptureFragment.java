package cafe.plastic.documentscanner.ui.fragments;

import android.Manifest;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;
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
import cafe.plastic.documentscanner.vision.PageDetector;
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
    private CaptureViewModel mViewModel;
    private Fotoapparat mFotoapparat;
    private CaptureFragmentBinding mCaptureFragmentBinding;
    private final ObjectTracker mObjectTracker = new ObjectTracker();
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private final PublishProcessor<Bitmap> mCapturedImages = PublishProcessor.create();
    private final Flowable<PageDetector.Region> mTrackerObserver = mObjectTracker.processedOutput();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mCaptureFragmentBinding = DataBindingUtil
                .inflate(inflater, R.layout.capture_fragment, container, false);
        mCaptureFragmentBinding.setHandlers(new Handlers());
        return mCaptureFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(requireActivity()).get(CaptureViewModel.class);
        mViewModel.flashMode.observe(this, f -> configureCamera());
        mViewModel.captureMode.observe(this, o -> configureCamera());
        mCaptureFragmentBinding.setHandlers(new Handlers());
        mCaptureFragmentBinding.setViewmodel(mViewModel);
        mCaptureFragmentBinding.setLifecycleOwner(this);
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        initializeCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        initializeCamera();
        mFotoapparat.start();
        configureObservers();
        configureCamera();
        animate(R.layout.capture_fragment_capturing_off, 400, new LinearInterpolator());
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
        mFotoapparat.stop();
        mCompositeDisposable.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        mObjectTracker.close();
    }

    private void configureObservers() {
        PublishProcessor<Boolean> mCaptureCycle = PublishProcessor.create();

        mCompositeDisposable.add(
                mCaptureCycle.switchMap(ignored -> {
                    Timber.d("Observable chain reset.");
                    return mTrackerObserver
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
                                mCaptureFragmentBinding.featureOverlay.updateRegion(current.value());
                                mCaptureFragmentBinding.featureOverlay.updateLockTime((float) acc / MAX_LOCK_TIME);
                                mViewModel.captureState.setValue(current.value().state);
                                return acc;
                            })
                            .filter(t -> t >= MAX_LOCK_TIME)
                            .withLatestFrom(mTrackerObserver.filter(r -> r.state == PageDetector.State.LOCKED), Pair::new)
                            .filter(t -> mViewModel.captureMode.getValue() == CameraState.CaptureMode.AUTO)
                            .take(1)
                            .map(i -> {
                                animate(R.layout.capture_fragment_capturing_on, 120, new LinearInterpolator());
                                mViewModel.captureState.setValue(PageDetector.State.CAPTURE);
                                return i;
                            })
                            .observeOn(Schedulers.computation())
                            .map(e -> {
                                Bitmap bitmap = mFotoapparat.takePicture().toBitmap().await().bitmap;
                                return mObjectTracker.processPhoto(bitmap, new PageDetector.Region(e.second));
                            })
                            .observeOn(AndroidSchedulers.mainThread());

                }).subscribe(b -> {
                    mCapturedImages.onNext(b);
                }));

        mCompositeDisposable.add(
                mCapturedImages
                        .observeOn(Schedulers.io())
                        .map(b -> {
                            Timber.d("Current thread: " + Thread.currentThread().getName());
                             return mViewModel.imageManager.storeTempBitmap(b);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> {
                            Timber.d("Image saved to: " + s);
                            Toast.makeText(getContext(), "Image saved to: " + s, Toast.LENGTH_SHORT).show();
                            animate(R.layout.capture_fragment_capturing_off, 120, new LinearInterpolator());
                            CaptureFragmentDirections.ConfirmAction action = CaptureFragmentDirections.confirmAction(s);
                            NavHostFragment.findNavController(CaptureFragment.this).navigate(action);

                            mCaptureCycle.onNext(true);
                        }));
        mCaptureCycle.onNext(true);
    }

    private void initializeCamera() {
        mFotoapparat = Fotoapparat.with(requireActivity())
                .into(mCaptureFragmentBinding.cameraView
                )
                .focusView(mCaptureFragmentBinding.focusView)
                .previewScaleType(ScaleType.CenterCrop)
                .frameProcessor(mObjectTracker)
                .build();
    }

    private void configureCamera() {
        CameraConfiguration.Builder builder = new CameraConfiguration.Builder();
        switch (mViewModel.flashMode.getValue()) {
            case OFF:
                builder.flash(FlashSelectorsKt.off());
                break;
            case ON:
                builder.flash(FlashSelectorsKt.torch());
                break;
            default:
                builder.flash(FlashSelectorsKt.off());
        }
        mFotoapparat.updateConfiguration(builder.build());
    }

    private void animate(int targetLayout, int time, TimeInterpolator interpolator) {
        ConstraintSet target = new ConstraintSet();
        ChangeBounds transition = new ChangeBounds();
        target.clone(getActivity(), targetLayout);
        transition.setInterpolator(interpolator);
        transition.setDuration(time);
        TransitionManager.beginDelayedTransition(mCaptureFragmentBinding.constraintLayout, transition);
        target.applyTo(mCaptureFragmentBinding.constraintLayout);
    }

    public class Handlers {

        public void onFlashButtonClicked(View view) {
            CameraState.FlashMode flash = mViewModel.flashMode.getValue();
            if (flash == CameraState.FlashMode.ON) {
                flash = CameraState.FlashMode.OFF;
            } else {
                flash = CameraState.FlashMode.ON;
            }
            mViewModel.flashMode.setValue(flash);
        }

        public void onCaptureModeButtonClicked(View view) {
            CameraState.CaptureMode outline = mViewModel.captureMode.getValue();
            if (outline == CameraState.CaptureMode.AUTO) {
                outline = CameraState.CaptureMode.MANUAL;

            } else {
                outline = CameraState.CaptureMode.AUTO;
            }
            mViewModel.captureMode.setValue(outline);
        }
    }
}

