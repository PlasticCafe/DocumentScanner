package cafe.plastic.pagedetect;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Objects;

public class PostProcess {
    private static native void Brightness(Bitmap input, Bitmap output, float brightness, float contrast, boolean same);

    private static native void Threshold(Bitmap input, Bitmap output);

    private Bitmap bitmap;
    private Bitmap renderTarget;
    private float brightness;
    private float contrast;
    private Quad region;
    private int rotation;
    private boolean dirty = false;

    public PostProcess(Bitmap bitmap, float scale, RenderConfiguration renderConfig) {
        int width = (int) (bitmap.getWidth() * scale);
        int height = (int) (bitmap.getHeight() * scale);
        if (width % 2 != 0) width += 1;
        if (height % 2 != 0) height += 1;
        this.bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        this.workingBitmap = this.bitmap.copy(this.bitmap.getConfig(), true);
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
        Brightness(bitmap, workingBitmap, (float) (config.brightness - 50) / 50.0f, config.contrast / 40.0f, false);
    }

    private void renderThreshold() {
        Threshold(workingBitmap, workingBitmap);
    }


    public static class RenderConfiguration implements Serializable {
        private Bitmap bitmap;
        private float brightness;
        private float contrast;
        private boolean threshold;
        private Quad region;
        private int rotation;

        public static class Builder {
            private Bitmap bitmap;
            private float brightness = 0;
            private float contrast = 0;
            private boolean threshold = false;
            private Quad region;
            private int rotation = 0;

            public Builder(Bitmap bitmap) {
                this.bitmap = bitmap;
            }

            public Builder brightness(int brightness) {
                this.brightness = brightness;
                return this;
            }

            public Builder contrast(int contrast) {
                this.contrast = contrast;
                return this;
            }

            public Builder threshold(boolean threshold) {
                this.threshold = threshold;
                return this;
            }

            public Builder region(Quad region) {
                this.region = region.copy();
                return this;
            }

            public Builder rotation(int rotation) {
                this.rotation = rotation;
                return this;
            }

            public RenderConfiguration build() {
                RenderConfiguration config = new RenderConfiguration();
                config.bitmap = this.bitmap;
                config.brightness = this.brightness;
                config.contrast = this.contrast;
                config.threshold = this.threshold;
                config.region = this.region;
                config.rotation = this.rotation;
                return config;
            }


        }
        private RenderConfiguration() {
            
        }
    }

}
