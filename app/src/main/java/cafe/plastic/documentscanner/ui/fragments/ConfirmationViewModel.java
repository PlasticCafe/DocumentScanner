package cafe.plastic.documentscanner.ui.fragments;

import android.app.Application;

import java.io.IOException;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import cafe.plastic.documentscanner.util.TempImageManager;
import cafe.plastic.pagedetect.PostProcess;

public class ConfirmationViewModel extends AndroidViewModel implements LifecycleObserver {
    public final PostProcess.RenderConfiguration config;

    public ConfirmationViewModel(Application application) {
        super(application);
        PostProcess.RenderConfiguration tempConfig;
        try {
            tempConfig = TempImageManager.getInstance(application.getApplicationContext()).loadTempBitmap();
        } catch (IOException e) {
            e.printStackTrace();
            tempConfig = null;
        }
        config = tempConfig;
    }
}
