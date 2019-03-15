package cafe.plastic.documentscanner.ui.data;

import android.content.SharedPreferences;

public class SharedIntLiveData extends SharedPrefsLiveData<Integer> {
    public SharedIntLiveData(SharedPreferences prefs, String key, Integer defaultValue) {
        super(prefs, key, defaultValue);
    }

    @Override
    protected Integer getPref() {
        return prefs.getInt(key, defaultValue);
    }

    @Override
    protected void setPref(Integer value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }
}
