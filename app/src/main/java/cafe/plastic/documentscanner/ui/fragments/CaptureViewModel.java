package cafe.plastic.documentscanner.ui.fragments;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import cafe.plastic.documentscanner.ui.data.SafeLiveData;
import cafe.plastic.documentscanner.vision.PageDetector;

public class CaptureViewModel extends AndroidViewModel implements LifecycleObserver {

    public final SafeLiveData<CameraState.FlashMode> flashMode = new SafeLiveData<>(CameraState.FlashMode.OFF);
    public final SafeLiveData<CameraState.CaptureMode> captureMode = new SafeLiveData<>(CameraState.CaptureMode.AUTO);
    public final MutableLiveData<PageDetector.Region>  captureState = new MutableLiveData<>();
    public final MutableLiveData<Bitmap> currentPhoto = new MutableLiveData<>();

    public CaptureViewModel(Application application) {
        super(application);
    }

}
