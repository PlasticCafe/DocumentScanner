package cafe.plastic.documentscanner.ui.fragments;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import cafe.plastic.documentscanner.ui.data.SafeLiveData;
import cafe.plastic.documentscanner.ui.data.SharedIntLiveData;
import cafe.plastic.pagedetect.PageDetector;

public class ConfirmationViewModel extends AndroidViewModel implements LifecycleObserver {
    public final SharedIntLiveData brightness;
    public final SharedIntLiveData contrast;
    public final LiveData<PageDetector.Region> image_region = new MutableLiveData<>();
    private final SharedPreferences prefs;
    public ConfirmationViewModel(@NonNull Application application) {
        super(application);
        prefs = PreferenceManager.getDefaultSharedPreferences(application);
        brightness = new SharedIntLiveData(prefs, ConfirmationFragment.PREF_IMAGE_BRIGHTNESS, 50);
        contrast = new SharedIntLiveData(prefs, ConfirmationFragment.PREF_IMAGE_CONTRAST, 1);
    }

}
