package cafe.plastic.documentscanner.ui;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;

import androidx.databinding.BindingAdapter;
import cafe.plastic.documentscanner.ui.fragments.CameraState;
import cafe.plastic.documentscanner.ui.fragments.CaptureFragment;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.parameter.Flash;
import io.fotoapparat.selector.FlashSelectorsKt;
import kotlin.jvm.functions.Function1;

public class Bindings {
    @BindingAdapter({"flashState", "flashOff", "flashOn", "flashAuto"})
    public static void updateFlashButton(ImageButton view, CameraState.Flash flashState, Drawable flashOff, Drawable flashOn, Drawable flashAuto) {
        Drawable drawable;
        switch (flashState) {
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
}
