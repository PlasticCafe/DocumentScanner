package cafe.plastic.pagedetect;

import android.graphics.Bitmap;

public class PostProcess {
    private static native void Brightness(Bitmap input, Bitmap output, float brightness, float contrast, boolean same);
    private static native void Threshold(Bitmap input, Bitmap output);
    private PostProcess() {

    }

    public static void brightness(Bitmap input, Bitmap output, float brightness, float contrast, boolean same) {
        Brightness(input, output, brightness, contrast, same);
    }

    public static void threshold(Bitmap input,  Bitmap output) {
        Threshold(input, output);
    }

}
