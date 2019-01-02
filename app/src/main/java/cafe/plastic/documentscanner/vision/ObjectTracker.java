package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

import io.fotoapparat.preview.Frame;
import io.fotoapparat.result.Photo;
import io.reactivex.Flowable;

public class ObjectTracker extends VisionFrameProcessor<PageDetector.Region> {
    private PageDetector mPageDetector = new PageDetector();

    @Override
    public Flowable<PageDetector.Region> processedOutput() {
        return mFrames.map(f -> processNative(f));
    }

    public Bitmap processPhoto(Photo photo, PageDetector.Region roi) {
        byte[] photoBytes = photo.encodedImage;
        Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
        double scale = (double) bitmap.getWidth() / roi.frameSize.getWidth();
        Matrix matrix = new Matrix();
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        roi.scale(scale);
        Point dims = roi.getDimensions();
        Bitmap processed = Bitmap.createBitmap(dims.x, dims.y, Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(processed);
        Matrix warp = new Matrix();
        float[] src = roi.toFloatArray();
        float[] dst = {
                0, 0,
                dims.x, 0,
                dims.x, dims.y,
                0, dims.y
        };
        warp.setPolyToPoly(src, 0, dst, 0, 4);
        c.drawBitmap(bitmap, warp, p);
        return processed;
    }

    public void close() {
        mPageDetector.release();
    }

    private PageDetector.Region processNative(Frame frame) {
        return mPageDetector.detect(frame.getImage(), frame.getSize().width, frame.getSize().height, frame.getRotation());
    }
}
