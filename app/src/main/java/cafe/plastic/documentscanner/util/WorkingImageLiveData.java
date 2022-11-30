package cafe.plastic.documentscanner.util;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.io.FileNotFoundException;
import java.io.IOException;

import cafe.plastic.pagedetect.PostProcess;

public class WorkingImageLiveData extends LiveData<PostProcess.RenderConfiguration> implements WorkingImageManager.WorkingImageListener {
    final WorkingImageManager manager;
    public WorkingImageLiveData(Context context) {
       manager = WorkingImageManager.getInstance(context.getApplicationContext());
        try {
            setValue(manager.loadTempBitmap());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActive() {
        super.onActive();
        try {
            manager.addListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        manager.removeListener(this);
    }

    public void updateConfig(PostProcess.RenderConfiguration config) {
        try {
            manager.storeTempImage(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteConfig() {
        try {
            manager.clearImage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageUpdated(PostProcess.RenderConfiguration config) {
        setValue(config);
    }
}
