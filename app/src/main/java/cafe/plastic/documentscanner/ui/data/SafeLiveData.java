package cafe.plastic.documentscanner.ui.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

//Mutable live data that returns a default value on null
public class SafeLiveData<T> extends MutableLiveData<T> {
    private final T mDefaultValue;

    public SafeLiveData(@NonNull T defaultValue) {
        mDefaultValue = defaultValue;
    }

    @NonNull
    @Override
    public T getValue() {
        T value = super.getValue();
        if(value == null) {
            value = mDefaultValue;
        }
        return value;
    }
}
