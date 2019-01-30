package cafe.plastic.documentscanner.ui.fragments;

import android.Manifest;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProviders;

import android.animation.TimeInterpolator;

import androidx.databinding.DataBindingUtil;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.ChangeBounds;

import android.util.LogPrinter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionManager;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.databinding.CaptureFragmentBinding;
import cafe.plastic.documentscanner.vision.ObjectTracker;
import cafe.plastic.documentscanner.vision.PageDetector;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.Photo;
import io.fotoapparat.selector.FlashSelectorsKt;
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
    private final PublishProcessor<Photo> mPhotoObserver = PublishProcessor.create();
    private float mCaptureTimeout = 1.5f;
    private float mCurrentCaptureTime = 0.0f;
    private boolean mLocked = false;
    private long mLockTime = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Timber.d("onCreateView");
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
        mCompositeDisposable.add(mObjectTracker.processedOutput()
                .observeOn(AndroidSchedulers.mainThread())
                .scan(new Pair<PageDetector.Region, Long>(new PageDetector.Region(), 0L), (acc, current) -> {
                    long diff = current.time - acc.first.time;
                    long time = acc.second;
                    if (current.state == PageDetector.State.LOCKED) {
                        time += diff;
                        if (time > MAX_LOCK_TIME) {
                            time = MAX_LOCK_TIME;
                        }
                    } else {
                        if (diff > time)
                            time = 0;
                        else
                            time -= diff;
                    }
                    mCaptureFragmentBinding.featureOverlay.updateRegion(current, (float) time / MAX_LOCK_TIME);
                    mViewModel.captureState.setValue(current);
                    if (current.state == PageDetector.State.LOCKED && time == MAX_LOCK_TIME) {
                        if (mViewModel.captureMode.getValue() == CameraState.CaptureMode.AUTO) {
                            mFotoapparat.focus();
                            capture();
                        } else {
                            mCaptureFragmentBinding.captureButton.setEnabled(true);
                        }
                    } else {
                        mCaptureFragmentBinding.captureButton.setEnabled(false);
                    }

                    return new Pair<PageDetector.Region, Long>(current, time);
                })
                .subscribe(p -> {
                    mCaptureFragmentBinding.featureOverlay.updateRegion(p.first, (float) p.second / MAX_LOCK_TIME);
                    mViewModel.captureState.setValue(p.first);
                    if (p.first.state == PageDetector.State.LOCKED && p.second == MAX_LOCK_TIME) {
                        if (mViewModel.captureMode.getValue() == CameraState.CaptureMode.AUTO) {
                            mFotoapparat.focus();
                            capture();
                        } else {
                            mCaptureFragmentBinding.captureButton.setEnabled(true);
                        }
                    } else {
                        mCaptureFragmentBinding.captureButton.setEnabled(false);
                    }
                }));

        mCompositeDisposable
                .add(mPhotoObserver.toObservable()
                        .withLatestFrom(mObjectTracker.processedOutput()
                                .toObservable()
                                .filter(r -> r.state == PageDetector.State.LOCKED), Pair::new)
                        .observeOn(Schedulers.computation())
                        .map(p -> {
                            mFotoapparat.stop();
                            return mObjectTracker.processPhoto(p.first, new PageDetector.Region(p.second), getContext());
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(b ->
                        {
                            Toast.makeText(getContext(), "Image saved.", Toast.LENGTH_SHORT).show();
                            mViewModel.currentPhoto.setValue(b);
                            mLocked = false;
                            mCaptureFragmentBinding.captureButton.setEnabled(false);
                            NavHostFragment.findNavController(CaptureFragment.this)
                                    .navigate(R.id.action_captureFragment_to_confirmFragment);
                        }));
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

        switch (mViewModel.captureMode.getValue()) {
            case AUTO:
                mCaptureFragmentBinding.captureButton.setEnabled(false);
                animate(R.layout.capture_fragment_capture_button_off, 100, new LinearInterpolator());
                break;
            case MANUAL:
                mCaptureFragmentBinding.captureButton.setEnabled(true);
                animate(R.layout.capture_fragment_capture_button_on, 100, new LinearInterpolator());
                break;
            default:
        }
        mFotoapparat.updateConfiguration(builder.build());
    }

    private void capture() {
        mFotoapparat.takePicture().toPendingResult().whenDone(photo -> {
            Timber.d("Got photo");
            if (photo != null) {
                mPhotoObserver.onNext(photo);
            }
        });
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

        public void onCaptureButtonClicked(View view) {
            capture();
        }
    }
}

