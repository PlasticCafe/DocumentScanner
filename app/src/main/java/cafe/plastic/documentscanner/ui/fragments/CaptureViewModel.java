package cafe.plastic.documentscanner.ui.fragments;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;

public class CaptureViewModel extends AndroidViewModel implements LifecycleObserver {

    public final MutableLiveData<CameraState.Flash> flashState = new MutableLiveData<>();
    public final MutableLiveData<CameraState.Outline> outlineState = new MutableLiveData<>();
    public final MutableLiveData<Bitmap> currentPhoto = new MutableLiveData<>();

    public CaptureViewModel(Application application) {
        super(application);
        flashState.setValue(CameraState.Flash.OFF);
        outlineState.setValue(CameraState.Outline.ON);
    }

}
