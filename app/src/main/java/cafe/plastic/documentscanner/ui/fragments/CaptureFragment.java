package cafe.plastic.documentscanner.ui.fragments;

import android.Manifest;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProviders;

import android.animation.TimeInterpolator;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.ChangeBounds;

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
    private CaptureViewModel mViewModel;
    private Fotoapparat mFotoapparat;
    private CaptureFragmentBinding mCaptureFragmentBinding;
    private boolean mCaptureLoading = false;
    private boolean mMenuOpen = false;
    private boolean mButtonsHidden = false;
    private final ObjectTracker mObjectTracker = new ObjectTracker();
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private final PublishProcessor<Photo> mPhotoObserver = PublishProcessor.create();
    private int lockCounter = 0;

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
        mViewModel.flashState.observe(this, f -> configureCamera());
        mViewModel.outlineState.observe(this, o -> configureCamera());
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
        initializeUI();
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
                .subscribe(region -> {
                    mCaptureFragmentBinding.featureOverlay.updateRegion(region);
                    if (lockCounter == -1) return;
                    if (region.state != PageDetector.State.LOCKED) {
                        lockCounter = 0;
                    } else {
                        lockCounter++;
                    }

                    if (lockCounter > 40) {
                        mFotoapparat.focus();
                        capture();
                        lockCounter = -1;
                    }
                }));

        mCompositeDisposable
                .add(mPhotoObserver.toObservable()
                        .withLatestFrom(mObjectTracker.processedOutput().toObservable()
                                        .filter(r -> r.state == PageDetector.State.LOCKED),
                                Pair::new)
                        .observeOn(Schedulers.computation())
                        .map(p -> {
                            mFotoapparat.stop();
                            return mObjectTracker.processPhoto(p.first, p.second, getContext());
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(b -> {
                            Toast.makeText(getContext(), "Image saved.", Toast.LENGTH_SHORT).show();
                            mViewModel.currentPhoto.setValue(b);
                            lockCounter = 0;
                            NavHostFragment.findNavController(CaptureFragment.this)
                                    .navigate(R.id.action_captureFragment_to_confirmFragment);
                            toggleCaptureLoading();
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
        if(mViewModel.flashState.getValue() != null) {
            switch (mViewModel.flashState.getValue()) {
                case OFF:
                    builder.flash(FlashSelectorsKt.off());
                    break;
                case ON:
                    builder.flash(FlashSelectorsKt.torch());
                    break;
                default:
                    builder.flash(FlashSelectorsKt.off());
            }
        }

        if(mViewModel.outlineState.getValue() != null) {
            switch (mViewModel.outlineState.getValue()) {
                case OFF:
                    break;
                case ON:
                    break;
                default:
            }
        }
        mFotoapparat.updateConfiguration(builder.build());
    }

    private void capture() {
        toggleCaptureLoading();
        mFotoapparat.takePicture().toPendingResult().whenDone(photo -> {
            Timber.d("Got photo");
            if(photo != null) {
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

    private void toggleMenu(boolean state, int time) {
        if (state)
            animate(R.layout.capture_fragment_menu_open, time, new LinearInterpolator());
        else
            animate(R.layout.capture_fragment_menu_closed, time, new LinearInterpolator());
        mMenuOpen = !mMenuOpen;
    }

    private void toggleButtons(boolean state, int time) {
        if (state)
            animate(R.layout.capture_fragment_buttons_shown, time, new LinearInterpolator());
        else
            animate(R.layout.capture_fragment_buttons_hidden, time, new LinearInterpolator());
        mButtonsHidden = !mButtonsHidden;
    }

    private void toggleCaptureLoading() {
        if (mCaptureLoading) {
            Timber.d("Hiding loading UI");
            mCaptureFragmentBinding.loadinggroup.setVisibility(View.GONE);
            toggleButtons(true, 100);
            mCaptureLoading = false;
        } else {
            Timber.d("Throwing up loading UI");
            mCaptureFragmentBinding.loadinggroup.setVisibility(View.VISIBLE);
            toggleButtons(false, 100);
            toggleMenu(false, 100);
            mCaptureLoading = true;
        }
    }

    private void initializeUI() {
        toggleButtons(true, 0);
        toggleMenu(false, 0);
    }


    public class Handlers {

        public void onCaptureButtonClicked(View view) {
            capture();
        }

        public void onFlashButtonClicked(View view) {
            CameraState.Flash flash = mViewModel.flashState.getValue();
            if (flash == CameraState.Flash.OFF) {
                flash = CameraState.Flash.ON;
            } else if (flash == CameraState.Flash.ON) {
                flash = CameraState.Flash.OFF;
            }
            mViewModel.flashState.setValue(flash);
        }

        public void onOutlineButtonClicked(View view) {
            CameraState.Outline outline = mViewModel.outlineState.getValue();
            if (outline == CameraState.Outline.OFF) {
                outline = CameraState.Outline.ON;
            } else {
                outline = CameraState.Outline.OFF;
            }
            mViewModel.outlineState.setValue(outline);
        }

        public void onMenuOpened(View view) {
            toggleMenu(true, 100);
            toggleButtons(false, 100);
        }

        public void onMenuClosed(View view) {
            toggleMenu(false, 100);
            toggleButtons(true, 100);
        }
    }
}

