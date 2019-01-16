package cafe.plastic.documentscanner.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.provider.MediaStore;

import io.fotoapparat.preview.Frame;
import io.fotoapparat.result.Photo;
import io.reactivex.Flowable;
import timber.log.Timber;

public class ObjectTracker extends VisionFrameProcessor<PageDetector.Region> {
    private final PageDetector mPageDetector = new PageDetector();

    public Flowable<PageDetector.Region> processedOutput() {
        return mFrames.map(this::processNative);
    }

    public Bitmap processPhoto(Photo photo, PageDetector.Region roi, Context context) {
        byte[] photoBytes = photo.encodedImage;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length, options);
        double scale = (double) bitmap.getWidth() / roi.frameSize.getWidth();
        Matrix matrix = new Matrix();
        roi.scale(scale);
        Point dims = roi.getDimensions();
        Bitmap processed = Bitmap.createBitmap(dims.x, dims.y, Bitmap.Config.RGB_565);
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
        mPageDetector.thresholdImage(processed);
        matrix.postRotate(-1*roi.rotation);
        processed = Bitmap.createBitmap(processed, 0, 0, processed.getWidth(), processed.getHeight(), matrix, false);
        String filename = "DocumentScanner" + System.currentTimeMillis() + ".jpg";
        MediaStore.Images.Media.insertImage(context.getContentResolver(), processed, filename, "Image Scan");
        Timber.d("Image saved");
        return processed;
    }

    public void close() {
        mPageDetector.release();
    }

    private PageDetector.Region processNative(Frame frame) {
        return mPageDetector.detect(frame.getImage(), frame.getSize().width, frame.getSize().height, frame.getRotation());
    }
}
