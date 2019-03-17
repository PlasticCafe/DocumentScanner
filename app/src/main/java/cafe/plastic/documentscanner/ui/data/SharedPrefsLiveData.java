package cafe.plastic.documentscanner.ui.data;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

abstract public class SharedPrefsLiveData<T> extends MutableLiveData<T> {
    SharedPreferences prefs;
    String key;
    T defaultValue;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    SharedPrefsLiveData(SharedPreferences prefs, String key, T defaultValue) {
        this.prefs = prefs;
        this.key = key;
        this.defaultValue = defaultValue;
        setValue(getPref());
        buildListener();
    }

    @Override
    public void setValue(T value) {
        setPref(value);
        super.setValue(value);
    }

    @Nullable
    @Override
    public T getValue() {
        return super.getValue();
    }

    protected abstract T getPref();

    protected abstract void setPref(T value);

    private void buildListener() {
        prefListener = (sharedPreferences, key) -> {
            if(SharedPrefsLiveData.this.key.equals(key)) {
                setValue(getPref());
            }
        };
    }
}
