package cafe.plastic.documentscanner.ui;

import android.graphics.drawable.Drawable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import cafe.plastic.documentscanner.ui.fragments.CameraState;
import cafe.plastic.pagedetect.PageDetector;

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

    @BindingAdapter({"captureState", "searchingText", "perspectiveText", "sizeText", "lockedText", "captureText"})
    public static void updateStatusText(TextView view, PageDetector.State captureState, String searchingText, String perspectiveText, String sizeText, String lockedText, String captureText) {
        if(captureState == null) return;
        switch(captureState) {
            case NONE:
                view.setText(searchingText);
                break;
            case PERSPECTIVE:
                view.setText(perspectiveText);
                break;
            case SIZE:
                view.setText(sizeText);
                break;
            case LOCKED:
                view.setText(lockedText);
                break;
            case CAPTURE:
                view.setText(captureText);
                break;
            default:
                view.setText(searchingText);
        }
    }
}

