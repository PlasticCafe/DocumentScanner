package cafe.plastic.documentscanner.ui;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;

import androidx.databinding.BindingAdapter;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.parameter.Flash;
import io.fotoapparat.selector.FlashSelectorsKt;
import kotlin.jvm.functions.Function1;

public class Bindings {
    @BindingAdapter({"flashMode", "flashOff", "flashOn", "flashAuto"})
    public static void updateFlashButton(ImageButton view, CameraConfiguration cameraConfig, Drawable flashOff, Drawable flashOn, Drawable flashAuto) {
        Drawable drawable;
        Function1<Iterable<? extends Flash>, Flash> flashMode = cameraConfig.getFlashMode();
        if (flashMode.equals(FlashSelectorsKt.on())) {
            drawable = flashOn;
        } else if(flashMode.equals(FlashSelectorsKt.autoFlash())) {
            drawable = flashAuto;
        } else {
            drawable = flashOff;
        }
        view.setImageDrawable(drawable);
    }
}
