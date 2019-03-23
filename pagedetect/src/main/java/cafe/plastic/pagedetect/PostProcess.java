package cafe.plastic.pagedetect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.Serializable;
import java.util.Objects;

public class PostProcess {
    private static native void Brightness(Bitmap input, Bitmap output, float brightness, float contrast, boolean same);

    private static native void Threshold(Bitmap input, Bitmap output);

    RenderConfiguration config;
    RenderConfiguration oldConfig;
    Bitmap scaledBitmap;
    Bitmap workingBitmap;
    private boolean dirty = false;

    public PostProcess(Bitmap bitmap, RenderConfiguration renderConfig) {
        this.config = renderConfig;
        dirty = true;
        createBitmaps();
    }

    public Bitmap render() {
        if (dirty) {
            if (oldConfig == null || config.bitmap != oldConfig.bitmap) {
                createBitmaps();
            }
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
            this.oldConfig = this.config;
            this.config = config;
        }
    }

    private void createBitmaps() {
        scaledBitmap = cropBitmap();
        workingBitmap = scaledBitmap.copy(scaledBitmap.getConfig(), true);
    }

    public Bitmap cropBitmap() {
        int width = (int) (config.bitmap.getWidth() * config.scale);
        int height = (int) (config.bitmap.getHeight() * config.scale);
        Bitmap tempScaledBitmap = Bitmap.createScaledBitmap(config.bitmap, width, height, true);
        Matrix matrix = new Matrix();
        Quad roi = config.region.copy();
        roi.scale(config.scale);
        Vec2 dims = roi.getDimensions();
        Bitmap processed = Bitmap.createBitmap((int)dims.getX(), (int)dims.getY(), Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(processed);
        Matrix warp = new Matrix();
        float[] src = roi.toFloatArray();
        float[] dst = {
                0, 0,
                dims.getX(), 0,
                dims.getX(), dims.getY(),
                0, dims.getY()
        };
        warp.setPolyToPoly(src, 0, dst, 0, 4);
        c.drawBitmap(tempScaledBitmap, warp, p);
        matrix.postRotate(-1*config.rotation);
        processed = Bitmap.createBitmap(processed, 0, 0, processed.getWidth(), processed.getHeight(), matrix, false);
        return processed;
    }

    private void renderBrightness() {
        Brightness(scaledBitmap, workingBitmap, (float) (config.brightness - 50) / 50.0f, config.contrast / 40.0f, false);
    }

    private void renderThreshold() {
        Threshold(workingBitmap, workingBitmap);
    }


    public static class RenderConfiguration {
        private Bitmap bitmap;
        private float scale;
        private float brightness;
        private float contrast;
        private boolean threshold;
        private Quad region;
        private int rotation;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RenderConfiguration that = (RenderConfiguration) o;
            return Float.compare(that.scale, scale) == 0 &&
                    Float.compare(that.brightness, brightness) == 0 &&
                    Float.compare(that.contrast, contrast) == 0 &&
                    threshold == that.threshold &&
                    rotation == that.rotation &&
                    Objects.equals(bitmap, that.bitmap) &&
                    Objects.equals(region, that.region);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bitmap, scale, brightness, contrast, threshold, region, rotation);
        }

        public static class Builder {
            private Bitmap bitmap;
            private float scale = 1.0f;
            private float brightness = 0;
            private float contrast = 0;
            private boolean threshold = false;
            private Quad region;
            private int rotation = 0;

            public Builder(Bitmap bitmap) {
                this.bitmap = bitmap;
            }

            public Builder scale(float scale) {
                this.scale = scale;
                return this;
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
                config.scale = this.scale;
                config.brightness = this.brightness;
                config.contrast = this.contrast;
                config.threshold = this.threshold;
                config.region = this.region;
                config.rotation = this.rotation;
                if (this.region == null) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    Quad region = new Quad();
                    region.set(0, 0, width, 0, width, height, 0, height);
                    config.region = region;
                }
                return config;
            }


        }

        private RenderConfiguration() {

        }
    }

}
