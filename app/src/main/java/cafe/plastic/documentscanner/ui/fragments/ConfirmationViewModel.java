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
    public final SafeLiveData<Integer> brightness = new SafeLiveData<>(50);
    public final SafeLiveData<Integer> contrast = new SafeLiveData<>(1);
    public ConfirmationViewModel(@NonNull Application application) {
        super(application);
    }

}
