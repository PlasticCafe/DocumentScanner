package cafe.plastic.documentscanner.ui.fragments;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;

public class CaptureViewModel extends AndroidViewModel implements LifecycleObserver {

    public final MutableLiveData<CaptureFragment.CameraState> cameraState;
    public final MutableLiveData<Bitmap> currentPhoto = new MutableLiveData<>();

    public CaptureViewModel(Application application) {
        super(application);
        cameraState = new MutableLiveData<>();
        cameraState.setValue(new CaptureFragment.CameraState(
                CaptureFragment.CameraState.Flash.OFF,
                CaptureFragment.CameraState.Focus.AUTO,
                CaptureFragment.CameraState.Outline.OFF
        ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

}
