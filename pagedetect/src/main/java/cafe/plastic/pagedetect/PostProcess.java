package cafe.plastic.pagedetect;

import android.graphics.Bitmap;

public class PostProcess {
    private static native void Brightness(Bitmap input, Bitmap output, float brightness);
    private PostProcess() {

    }

    public static void brightness(Bitmap input, Bitmap output, float brightness) {
        Brightness(input, output, brightness);
    }

}
