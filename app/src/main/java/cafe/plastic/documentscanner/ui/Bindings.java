package cafe.plastic.documentscanner.ui;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;

import androidx.databinding.BindingAdapter;
import cafe.plastic.documentscanner.ui.fragments.CaptureFragment;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.parameter.Flash;
import io.fotoapparat.selector.FlashSelectorsKt;
import kotlin.jvm.functions.Function1;

public class Bindings {
    @BindingAdapter({"cameraState", "flashOff", "flashOn", "flashAuto"})
    public static void updateFlashButton(ImageButton view, CaptureFragment.CameraState cameraState, Drawable flashOff, Drawable flashOn, Drawable flashAuto) {
        Drawable drawable;
        switch (cameraState.flash) {
            case ON:
                drawable = flashOn;
                break;
            case OFF:
                drawable = flashOff;
                break;
            case AUTO:
                drawable = flashAuto;
                break;
            default:
                drawable = flashOff;
        }
        view.setImageDrawable(drawable);
    }

    @BindingAdapter({"cameraState", "focusAuto", "focusFixed"})
    public static void updateFocusButton(ImageButton view, CaptureFragment.CameraState cameraState, Drawable focusAuto, Drawable focusFixed) {
        Drawable drawable;
        switch (cameraState.focus) {
            case AUTO:
                drawable = focusAuto;
                break;
            case FIXED:
                drawable = focusFixed;
                break;
            default:
                drawable = focusFixed;
        }
        view.setImageDrawable(drawable);
    }
}
