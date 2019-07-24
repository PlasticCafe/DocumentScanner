package cafe.plastic.documentscanner.ui.fragments;

import android.app.Application;

import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;

import cafe.plastic.documentscanner.util.WorkingImageLiveData;
import cafe.plastic.pagedetect.PostProcess;

public class ConfirmationViewModel extends AndroidViewModel implements LifecycleObserver {

    public WorkingImageLiveData workingImage;

    public ConfirmationViewModel(Application application) {
        super(application);
        workingImage = new WorkingImageLiveData(application);
    }

    public void updateConfig(PostProcess.RenderConfiguration config) {
        workingImage.updateConfig(config);

    }


    public void updateBrightness(float brightness) {
        PostProcess.RenderConfiguration config = duplicateConfig();
        if (config != null) {
            config.brightness = brightness;
            updateConfig(config);
        }
    }

    public void updateContrast(float contrast) {
        PostProcess.RenderConfiguration config = duplicateConfig();
        if (config != null) {
            config.contrast = contrast;
            updateConfig(config);
        }
    }

    public void updateThreshold(boolean threshold) {

        PostProcess.RenderConfiguration config = duplicateConfig();
        if (config != null) {
            config.threshold = threshold;
            updateConfig(config);
        }
    }

    public void updateRotation() {
        PostProcess.RenderConfiguration config = duplicateConfig();
        if (config != null) {
            config.rotation = (config.rotation + 90) % 360;
            updateConfig(config);
        }
    }

    private PostProcess.RenderConfiguration duplicateConfig() {
        if (workingImage.getValue() != null)
            return new PostProcess.RenderConfiguration.Builder(workingImage.getValue()).build();
        else
            return null;
    }

}
