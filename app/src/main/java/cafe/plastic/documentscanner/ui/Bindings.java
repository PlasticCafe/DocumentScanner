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
    @BindingAdapter({"flashState", "flashOff", "flashOn"})
    public static void updateFlashButton(ImageButton view, CameraState.Flash flashState, Drawable flashOff, Drawable flashOn) {
        if(flashState == CameraState.Flash.ON)
            view.setImageDrawable(flashOn);
        else
            view.setImageDrawable(flashOff);
    }
}
