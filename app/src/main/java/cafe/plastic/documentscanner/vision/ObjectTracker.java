package cafe.plastic.documentscanner.vision;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import io.fotoapparat.preview.Frame;
import io.reactivex.Flowable;

public class ObjectTracker extends VisionFrameProcessor<List<Rect>> {
    private List<Rect> trackingRegions = new ArrayList<>();
    private boolean initialized = false;
    private PageDetector mPageDetector = new PageDetector();

    @Override
    public Flowable<List<Rect>> processedOutput() {
        return mFrames.map(f -> processNative(f));
    }

    public void setTrackingRegion(Rect rect) {
        trackingRegions.clear();
        trackingRegions.add(rect);
    }

    public void close() {
        mPageDetector.release();
    }

    private List<Rect> processNative(Frame frame) {
        if (trackingRegions.size() > 0) {
            mPageDetector.initialize(frame, trackingRegions.get(0));
            trackingRegions.clear();
            initialized = true;
        }
        if (initialized) {
            return mPageDetector.detect(frame);
        } else {
            return new ArrayList<>();
        }
    }
}
