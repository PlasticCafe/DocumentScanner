package cafe.plastic.documentscanner.ui.fragments;

import android.Manifest;

import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProviders;

import android.animation.TimeInterpolator;
import android.content.Context;

import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.transition.ChangeBounds;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;


import java.util.List;

import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionManager;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.databinding.CaptureFragmentBinding;
import cafe.plastic.documentscanner.vision.ObjectTracker;
import cafe.plastic.documentscanner.vision.PageDetector;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.parameter.Flash;
import io.fotoapparat.parameter.FocusMode;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.result.Photo;
import io.fotoapparat.selector.FlashSelectorsKt;
import io.fotoapparat.selector.FocusModeSelectorsKt;
import io.fotoapparat.selector.PreviewFpsRangeSelectorsKt;
import io.fotoapparat.selector.ResolutionSelectorsKt;
import io.fotoapparat.view.CameraView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import kotlin.jvm.functions.Function1;
import timber.log.Timber;

public class CaptureFragment extends Fragment {
    private static final String TAG = CaptureFragment.class.getSimpleName();
    private CaptureViewModel mViewModel;
    private Fotoapparat mFotoapparat;
    private CameraConfiguration mCameraConfiguration;
    private CaptureFragmentBinding mCaptureFragmentBinding;
    private boolean mCaptureLoading = false;
    private boolean mMenuOpen = false;
    private boolean mButtonsHidden = false;
    private ObjectTracker mObjectTracker = new ObjectTracker();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private PublishProcessor<Photo> mPhotoObserver = PublishProcessor.create();
    private int lockCounter = 0;

    public static class CameraState {
        public Flash flash;
        public Focus focus;
        public Outline outline;

        public CameraState(Flash flash, Focus focus, Outline outline) {
            this.flash = flash;
            this.focus = focus;
            this.outline = outline;
        }

        public enum Flash {
            OFF,
            ON,
            AUTO
        }

        public enum Focus {
            AUTO,
            FIXED
        }

        public enum Outline {
            ON,
            OFF
        }
    }

    public static CaptureFragment newInstance() {
        return new CaptureFragment();
    }

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
        Timber.d("onActivityCreated");
        mViewModel = ViewModelProviders.of(getActivity()).get(CaptureViewModel.class);
        mViewModel.cameraState.observe(this, state -> rebuildConfig(state));
        mCaptureFragmentBinding.setViewmodel(mViewModel);
        mCaptureFragmentBinding.setLifecycleOwner(this);
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
        mFotoapparat = Fotoapparat.with(getActivity())
                .into((CameraView) getView().findViewById(R.id.camera_view))
                .previewScaleType(ScaleType.CenterCrop)
                .previewResolution(ResolutionSelectorsKt.highestResolution())
                .frameProcessor(mObjectTracker)
                .build();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
    }


    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        mFotoapparat.start();
        updateConfiguration();
        mCompositeDisposable.add(mObjectTracker.processedOutput()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(region -> {
                    mCaptureFragmentBinding.featureOverlay.updateRoi(region.roi);
                    switch (region.state) {
                        case NONE:
                            lockCounter = 0;
                            break;
                        case PERSPECTIVE:
                            lockCounter = 0;
                            break;
                        case SIZE:
                            lockCounter = 0;
                            break;
                        case LOCKED:
                            lockCounter++;
                            Timber.d("Ready for main region capture");
                    }
                    if (lockCounter > 20) {
                        capture();
                    }
                }));
        mCompositeDisposable.add(mObjectTracker.getFrames()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(f -> {
                    mCaptureFragmentBinding.featureOverlay.setFrameResolution(new Point(f.getSize().width, f.getSize().height));
                    mCaptureFragmentBinding.featureOverlay.setCurrentRotation(f.getRotation());
                }));
        mCompositeDisposable
                .add(Observable
                        .combineLatest(mPhotoObserver.toObservable(),
                                mObjectTracker.processedOutput().toObservable()
                                        .filter(r -> r.state == PageDetector.State.LOCKED),
                                mObjectTracker.getFrames().toObservable(),
                                this::processPhoto)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(b -> {
                            mViewModel.currentPhoto.setValue(b);
                            NavHostFragment.findNavController(CaptureFragment.this)
                                    .navigate(R.id.action_captureFragment_to_confirmFragment);
                            toggleCaptureLoading();
                        }));
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
        mFotoapparat.stop();
        mCompositeDisposable.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        mObjectTracker.close();
    }

    private void rebuildConfig(CameraState cameraState) {
        CameraConfiguration.Builder builder = new CameraConfiguration.Builder();
        Function1<Iterable<? extends Flash>, Flash> flashMode;
        Function1<Iterable<? extends FocusMode>, FocusMode> focusMode;
        switch (cameraState.flash) {
            case OFF:
                Timber.d("Turning off flash");
                flashMode = FlashSelectorsKt.off();
                break;
            case ON:
                Timber.d("Turning on torch");
                flashMode = FlashSelectorsKt.torch();
                break;
            case AUTO:
                Timber.d("Setting flash to auto");
                flashMode = FlashSelectorsKt.autoFlash();
                break;
            default:
                flashMode = FlashSelectorsKt.off();
        }

        switch (cameraState.focus) {
            case AUTO:
                Timber.d("Setting focus to automatic");
                focusMode = FocusModeSelectorsKt.autoFocus();
                break;
            case FIXED:
                Timber.d("Setting focus to fixed");
                focusMode = FocusModeSelectorsKt.fixed();
                break;
            default:
                focusMode = FocusModeSelectorsKt.fixed();
        }
        Timber.d("Rebuilding config");
        mCameraConfiguration = builder.previewResolution(ResolutionSelectorsKt.highestResolution()).build();
        updateConfiguration();
    }

    private void updateConfiguration() {
        if (mViewModel.cameraState.getValue() != null) {
            Timber.d("Reconfiguring camera");
            mFotoapparat.updateConfiguration(mCameraConfiguration);
        } else {
            Timber.d("Configuration not found");
        }
    }

    private void capture() {
        toggleCaptureLoading();
        mFotoapparat.takePicture().toPendingResult().whenDone(photo -> {
            Log.d(TAG, "Got photo");
            mPhotoObserver.onNext(photo);
        });
    }


    public class Handlers {
        private Context mContext;

        public Handlers() {
            mContext = CaptureFragment.this.getContext();
        }

        public void onCaptureButtonClicked(View view) {
            capture();
        }

        public void onFlashButtonClicked(View view) {
            CameraState cameraState = mViewModel.cameraState.getValue();
            switch (cameraState.flash) {
                case OFF:
                    cameraState.flash = CameraState.Flash.ON;
                    break;
                case ON:
                    cameraState.flash = CameraState.Flash.AUTO;
                    break;
                case AUTO:
                    cameraState.flash = CameraState.Flash.OFF;
                    break;
                default:
                    cameraState.flash = CameraState.Flash.OFF;
                    break;
            }
            mViewModel.cameraState.setValue(cameraState);
        }

        public void onFocusButtonClicked(View view) {
            CameraState cameraState = mViewModel.cameraState.getValue();
            switch (cameraState.focus) {
                case AUTO:
                    cameraState.focus = CameraState.Focus.FIXED;
                    break;
                case FIXED:
                    cameraState.focus = CameraState.Focus.AUTO;
                    break;
                default:
                    cameraState.focus = CameraState.Focus.FIXED;
            }
            mViewModel.cameraState.setValue(cameraState);
        }

        public void onMenuOpened(View view) {
            toggleMenu(true);
            toggleButtons(false);
        }

        public void onMenuClosed(View view) {
            toggleMenu(false);
            toggleButtons(true);
        }
    }

    private void toggleMenu(boolean state) {
        if (state)
            animate(R.layout.capture_fragment_menu_open, 400, new AnticipateOvershootInterpolator(1.0f));
        else
            animate(R.layout.capture_fragment_menu_closed, 400, new AnticipateOvershootInterpolator(1.0f));
        mMenuOpen = !mMenuOpen;
    }

    private void toggleButtons(boolean state) {
        if (state)
            animate(R.layout.capture_fragment_buttons_shown, 400, new AnticipateOvershootInterpolator(1.0f));
        else
            animate(R.layout.capture_fragment_buttons_hidden, 400, new AnticipateOvershootInterpolator(1.0f));
        mButtonsHidden = !mButtonsHidden;
    }

    private void animate(int targetLayout, long time, TimeInterpolator interpolator) {
        ConstraintSet target = new ConstraintSet();
        ChangeBounds transition = new ChangeBounds();
        target.clone(getActivity(), targetLayout);
        transition.setInterpolator(interpolator);
        transition.setDuration(time);
        TransitionManager.beginDelayedTransition(mCaptureFragmentBinding.constraintLayout, transition);
        target.applyTo(mCaptureFragmentBinding.constraintLayout);
    }

    private void toggleCaptureLoading() {
        if (mCaptureLoading) {
            Timber.d("Hiding loading UI");
            mCaptureFragmentBinding.loadinggroup.setVisibility(View.GONE);
            toggleButtons(true);
            mCaptureLoading = false;
        } else {
            Timber.d("Throwing up loading UI");
            mCaptureFragmentBinding.loadinggroup.setVisibility(View.VISIBLE);
            toggleButtons(false);
            toggleMenu(false);
            mCaptureLoading = true;
        }
    }

    private Bitmap processPhoto(Photo photo, PageDetector.Region roi, Frame frame) {
        byte[] photoBytes = photo.encodedImage;
        int rotation = photo.rotationDegrees;
        List<Point> points = roi.roi;
        Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        double scale = (double)bitmap.getWidth()/ frame.getSize().width;
        Matrix matrix = new Matrix();
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        roi.scale(scale);
        Point dims = roi.getDimensions();
        Bitmap processed = Bitmap.createBitmap(dims.x, dims.y, Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(processed);
        Matrix warp = new Matrix();
        float[] src = roi.toFloatArray();
        float[] dst = {
                0, 0,
                dims.x, 0,
                dims.x, dims.y,
                0, dims.y
        };
        warp.setPolyToPoly(src, 0, dst, 0, 4);
        c.drawBitmap(bitmap, warp, p);
        return processed;
    }
}

