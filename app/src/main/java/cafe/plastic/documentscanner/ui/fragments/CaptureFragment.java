package cafe.plastic.documentscanner.ui.fragments;

import android.Manifest;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;

import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.databinding.CaptureFragmentBinding;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.parameter.Flash;
import io.fotoapparat.parameter.camera.convert.FlashConverterKt;
import io.fotoapparat.selector.FlashSelectorsKt;
import io.fotoapparat.view.CameraView;
import kotlin.jvm.functions.Function1;
import timber.log.Timber;

import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.on;

public class CaptureFragment extends Fragment {
    private static final String TAG = CaptureFragment.class.getSimpleName();
    private CaptureViewModel mViewModel;
    private Fotoapparat mFotoapparat;
    private CaptureFragmentBinding mCaptureFragmentBinding;

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
        setRetainInstance(true);
        mViewModel = ViewModelProviders.of(this).get(CaptureViewModel.class);
        mCaptureFragmentBinding.setViewmodel(mViewModel);
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
        mFotoapparat = Fotoapparat.with(getActivity())
                .into((CameraView) getView().findViewById(R.id.camera_view))
                .build();
        mViewModel.cameraConfiguration.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mFotoapparat.updateConfiguration(((ObservableField<CameraConfiguration>) sender).get());
                Timber.d("Configuration update callback called");
            }
        });
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
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
        mFotoapparat.stop();
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
    }

    public class Handlers {
        private Context mContext;

        public Handlers() {
            mContext = CaptureFragment.this.getContext();
        }

        public void onCaptureButtonClicked(View view) {
            Toast.makeText(mContext, "Capture clicked", Toast.LENGTH_LONG).show();
            mFotoapparat.takePicture().toPendingResult().whenDone(photo -> {
                Log.d(TAG, "Got photo");
                byte[] photoBytes = photo.encodedImage;
                int rotation = photo.rotationDegrees;
                Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation * -1);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                Bundle args = ConfirmationFragment
                        .getArguments(rotatedBitmap, rotation);
                NavHostFragment
                        .findNavController(CaptureFragment.this)
                        .navigate(R.id.action_captureFragment_to_confirmFragment, args);
            });
        }

        public void onFlashButtonClicked(View view) {
            CameraConfiguration currentConfig = mViewModel.cameraConfiguration.get();
            Function1<Iterable<? extends Flash>, Flash> flashMode = mViewModel.cameraConfiguration.get().getFlashMode();
            CameraConfiguration.Builder configBuilder = new CameraConfiguration.Builder();
            currentConfig.
            if (mode.equals(FlashSelectorsKt.on().invoke(null))) {
                configBuilder.flash(FlashSelectorsKt.autoFlash());
            } else if (mode.equals(FlashSelectorsKt.off().invoke(null))) {
                configBuilder.flash(FlashSelectorsKt.on());
            } else {
                configBuilder.flash(FlashSelectorsKt.off());
                Timber.d("onFlashButtonClicked: Default condition chosen");
            }

            mViewModel.cameraConfiguration.set(configBuilder.build());
        }


    }
}

            configBuilder.focusMode(currentConfig.getFocusMode());