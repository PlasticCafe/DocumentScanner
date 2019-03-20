package cafe.plastic.pagedetect;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Objects;

public class PostProcess {
    private static native void Brightness(Bitmap input, Bitmap output, float brightness, float contrast, boolean same);

    private static native void Threshold(Bitmap input, Bitmap output);

    private Bitmap originalBitmap;
    private Bitmap workingBitmap;
    private RenderConfiguration config;
    private boolean dirty = false;

    public PostProcess(Bitmap bitmap, float scale, RenderConfiguration renderConfig) {
        int width = (int) (bitmap.getWidth() * scale);
        int height = (int) (bitmap.getHeight() * scale);
        if (width % 2 != 0) width += 1;
        if (height % 2 != 0) height += 1;
        this.originalBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        this.workingBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        this.config = renderConfig;

    }

    public Bitmap render() {
        if (dirty) {
            renderBrightness();
            if (config.threshold)
                renderThreshold();
            dirty = false;
        }
        return workingBitmap;
    }

    public void updateRenderConfig(RenderConfiguration config) {
        if (this.config != config) {
            this.dirty = true;
            this.config = config;
        }
    }

    private void renderBrightness() {
        Brightness(originalBitmap, workingBitmap, (float) (config.brightness - 50) / 50.0f, config.contrast / 40.0f, false);
    }

    private void renderThreshold() {
        Threshold(workingBitmap, workingBitmap);
    }


    public static class RenderConfiguration implements Serializable {
        final float brightness;
        final float contrast;
        final boolean threshold;
        final PageDetector.Region region;

        public RenderConfiguration(float brightness, float contrast, boolean threshold, PageDetector.Region region) {
            this.brightness = brightness;
            this.contrast = contrast;
            this.threshold = threshold;
            this.region = region;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RenderConfiguration that = (RenderConfiguration) o;
            return brightness == that.brightness &&
                    contrast == that.contrast &&
                    threshold == that.threshold;
        }

        @Override
        public int hashCode() {
            return Objects.hash(brightness, contrast, threshold);
        }
    }

}
