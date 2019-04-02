package cafe.plastic.pagedetect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Objects;

public class PostProcess {
    private static native void Brightness(Bitmap input, Bitmap output, float brightness, float contrast, boolean same);

    private static native void Threshold(Bitmap input, Bitmap output);

    RenderConfiguration config;
    Bitmap scaledBitmap;
    Bitmap workingBitmap;
    private boolean renderBitmaps = true;
    private boolean renderBrightness = true;
    private boolean renderThreshold = true;

    public PostProcess(RenderConfiguration renderConfig) {
        this.config = renderConfig;

    }

    public void updateConfig(RenderConfiguration config) {
        if(this.config.bitmap != config.bitmap ||
                !this.config.region.equals(config.region) ||
                this.config.rotation != config.rotation ||
                this.config.scale != config.scale)
            renderBitmaps = true;
        if(this.config.brightness != config.brightness || this.config.contrast != config.contrast)
            renderBrightness = true;
        if(this.config.threshold != config.threshold)
            renderThreshold = true;
        this.config = config;
    }

    public Bitmap render() {
        if(renderBitmaps) {
            createBitmaps();
            renderBrightness();
            renderThreshold();
        }
        else if(renderBrightness || renderThreshold) {
            renderBrightness();
            renderThreshold();
        }
        renderBitmaps = false;
        renderBrightness = false;
        renderThreshold = false;

        return workingBitmap;
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
        Bitmap processed = Bitmap.createBitmap((int) dims.getX(), (int) dims.getY(), Bitmap.Config.ARGB_8888);
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
        matrix.postRotate(-1 * config.rotation);
        processed = Bitmap.createBitmap(processed, 0, 0, processed.getWidth(), processed.getHeight(), matrix, false);
        return processed;
    }

    private void renderBrightness() {
        Brightness(scaledBitmap, workingBitmap, (float) (config.brightness - 50) / 50.0f, config.contrast / 40.0f, false);
    }

    private void renderThreshold() {
        if (config.threshold)
            Threshold(workingBitmap, workingBitmap);
    }


    public static class RenderConfiguration {
        public Bitmap bitmap;
        public float scale;
        public float brightness;
        public float contrast;
        public boolean threshold;
        public int rotation;
        public Quad region;

        public void writeToFile(File file) throws IOException {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));
            float[] regionArray = region.toFloatArray();
            outputStream.writeFloat(scale);
            outputStream.writeFloat(brightness);
            outputStream.writeFloat(contrast);
            outputStream.writeBoolean(threshold);
            outputStream.writeInt(rotation);
            for (float coord : regionArray) {
                outputStream.writeFloat(coord);
            }
            outputStream.close();
        }

        public static RenderConfiguration readFromFile(File file, Bitmap bitmap) throws IOException {
            RenderConfiguration.Builder builder = new RenderConfiguration.Builder(bitmap);
            DataInputStream inputStream = new DataInputStream(new FileInputStream(file));
            builder.scale(inputStream.readFloat());
            builder.brightness(inputStream.readFloat());
            builder.contrast(inputStream.readFloat());
            builder.threshold(inputStream.readBoolean());
            builder.rotation(inputStream.readInt());
            Quad region = new Quad();
            ArrayList<Vec2> regionArray = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                float x = inputStream.readFloat();
                float y = inputStream.readFloat();
                regionArray.add(new Vec2(x, y));
            }
            region.set(regionArray);
            builder.region(region);
            inputStream.close();
            return builder.build();
        }

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

            public Builder brightness(float brightness) {
                this.brightness = brightness;
                return this;
            }

            public Builder contrast(float contrast) {
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
