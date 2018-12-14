package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.opencv.android.Utils;
import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.KAZE;
import org.opencv.imgproc.Imgproc;
import org.opencv.tracking.MultiTracker;
import org.opencv.tracking.Tracker;
import org.opencv.tracking.TrackerCSRT;
import org.opencv.tracking.TrackerMOSSE;
import org.opencv.xfeatures2d.SIFT;
import org.opencv.xfeatures2d.SURF;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

public class ObjectTracker extends VisionFrameProcessor<List<Rect>> {
    private List<Rect> trackingRegions = new ArrayList<>();
    private Tracker mTracker;

    @Override
    public Flowable<List<Rect>> processedOutput() {
        return mPreparedFrames.map(m -> processFrame(m));
    }

    public Flowable<Bitmap> processedFrames() {
        return mPreparedFrames.map(m -> matToBitmap(m));
    }

    public void setTrackingRegion(Rect rect) {
        trackingRegions.clear();
        trackingRegions.add(rect);
    }

    private Bitmap matToBitmap(Mat mat) {
        for (Rect rect : trackingRegions) {
            Imgproc.rectangle(mat, new Point(rect.left, rect.top), new Point(rect.right, rect.bottom), new Scalar(0, 255, 0));
        }
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        mat.release();
        return bitmap;
    }

    private List<Rect> processFrame(Mat mat) {
        ArrayList<Rect> results = new ArrayList<>();
        if (trackingRegions.size() > 0) {
            if (mTracker == null) {
                mTracker = TrackerCSRT.create();
            }
            mTracker.clear();
            Rect r = trackingRegions.get(0);
            r.
            mTracker.init(mat, new Rect2d(r.left, r.top, r.right - r.left, r.bottom - r.top));
            trackingRegions.clear();
        } else if (mTracker != null) {
            Rect2d bbox = new Rect2d();
            if (mTracker.update(mat, bbox))
                results.add(new Rect((int) bbox.x, (int) bbox.y, (int) (bbox.x + bbox.width), (int) (bbox.y + bbox.height)));
        }
        return results;
    }


}
