package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import cafe.plastic.pagedetect.PageDetector;
import cafe.plastic.pagedetect.Vec2;
import io.fotoapparat.preview.Frame;
import io.reactivex.Flowable;

public class ObjectTracker extends VisionFrameProcessor<PageDetector.Region> {
    private final PageDetector mPageDetector = new PageDetector();

    public Flowable<PageDetector.Region> processedOutput() {
        return mFrames.map(this::processNative);
    }

    public Bitmap processPhoto(Bitmap bitmap, PageDetector.Region region) {
        float scale =  (float)bitmap.getWidth() / region.frameSize.getWidth();
        Matrix matrix = new Matrix();
        region.roi.scale(scale);
        Vec2 dims = region.roi.getDimensions();
        Bitmap processed = Bitmap.createBitmap((int)dims.getX(), (int)dims.getY(), Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(processed);
        Matrix warp = new Matrix();
        float[] src = region.roi.toFloatArray();
        float[] dst = {
                0, 0,
                dims.getX(), 0,
                dims.getX(), dims.getY(),
                0, dims.getY()
        };
        warp.setPolyToPoly(src, 0, dst, 0, 4);
        c.drawBitmap(bitmap, warp, p);
       // mPageDetector.thresholdImage(processed);
        matrix.postRotate(-1*region.rotation);
        processed = Bitmap.createBitmap(processed, 0, 0, processed.getWidth(), processed.getHeight(), matrix, false);
        return processed;
    }

    public void close() {
        mPageDetector.release();
    }

    private PageDetector.Region processNative(Frame frame) {
        return mPageDetector.detect(frame.getImage(), frame.getSize().width, frame.getSize().height, frame.getRotation());
    }
}
