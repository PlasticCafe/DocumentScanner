package cafe.plastic.documentscanner.ui;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;

import androidx.databinding.BindingAdapter;
import cafe.plastic.documentscanner.ui.fragments.CameraState;

public class Bindings {
    @BindingAdapter({"flashMode", "flashOnIcon", "flashOffIcon"})
    public static void updateFlashButton(ImageButton view, CameraState.FlashMode flashMode, Drawable flashOn, Drawable flashOff) {
        if (flashMode == CameraState.FlashMode.ON)
            view.setImageDrawable(flashOn);
        else
            view.setImageDrawable(flashOff);
    }

    @BindingAdapter({"captureMode", "captureAutoIcon", "captureManualIcon"})
    public static void updateOutlineButton(ImageButton view, CameraState.CaptureMode captureMode, Drawable captureAuto, Drawable captureManual) {
        if (captureMode == CameraState.CaptureMode.AUTO)
            view.setImageDrawable(captureAuto);
        else
            view.setImageDrawable(captureManual);
    }
}

