package cafe.plastic.documentscanner.ui.fragments;

import android.app.Application;

import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import cafe.plastic.documentscanner.R;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.selector.FlashSelectorsKt;
import io.fotoapparat.selector.FocusModeSelectorsKt;

public class CaptureViewModel extends AndroidViewModel implements LifecycleObserver {

    public final ObservableField<Integer> flashStatus = new ObservableField<>(0);
    public final ObservableField<CameraConfiguration> cameraConfiguration = new ObservableField<>();
    public CaptureViewModel(Application application) {
        super(application);
        CameraConfiguration cameraConfiguration = new CameraConfiguration.Builder()
                .flash(FlashSelectorsKt.off())
                .focusMode(FocusModeSelectorsKt.autoFocus())
                .build();
        this.cameraConfiguration.set(cameraConfiguration);
    }

}
