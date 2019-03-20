package cafe.plastic.documentscanner.ui.data;

import android.content.SharedPreferences;

public class SharedBoolLiveData extends SharedPrefsLiveData<Boolean> {
    SharedBoolLiveData(SharedPreferences prefs, String key, Boolean defaultValue) {
        super(prefs, key, defaultValue);
    }

    @Override
    protected Boolean getPref() {
        return prefs.getBoolean(key, defaultValue);
    }

    @Override
    protected void setPref(Boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
