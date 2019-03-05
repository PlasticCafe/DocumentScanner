package cafe.plastic.documentscanner.ui.fragments;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import cafe.plastic.documentscanner.ui.data.SafeLiveData;

public class ConfirmationViewModel extends AndroidViewModel implements LifecycleObserver {
    public final SafeLiveData<Integer> brightness = new SafeLiveData<>(50);
    public final SafeLiveData<Integer> contrast = new SafeLiveData<>(1);
    public ConfirmationViewModel(@NonNull Application application) {
        super(application);
    }
}
