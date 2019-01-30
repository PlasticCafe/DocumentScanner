package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

import cafe.plastic.documentscanner.util.Quad;
import cafe.plastic.documentscanner.util.Vec2;

public class PageDetector {
    private boolean mReleased = false;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private long mHandle;


    private native long Create();
    private native void Release();
    private native ArrayList<Vec2> GetRoi(byte[] frame, int width, int height);
    private native float GetArea(ArrayList<Vec2> vecs);
    private native float GetDistortion(ArrayList<Vec2> vecs);
    private native void ThresholdImage(Bitmap input);
    public enum State {
        LOCKED,
        PERSPECTIVE,
        SIZE,
        NONE
    }

    public static class Region {
        public final State state;
        public final Quad roi;
        public final Size frameSize;
        public final long time;
        public final int rotation;
        public Region() {
            this.state = State.NONE;
            this.roi = new Quad();
            this.frameSize = new Size(0, 0);
            this.rotation = 0;
            this.time = System.currentTimeMillis();
        }
        public Region(State state, Quad roi, Size frameSize, long time, int rotation) {
            this.state = state;
            this.roi = roi;
            this.frameSize = frameSize;
            this.time = time;
            this.rotation = rotation;
        }

        public Region(Region region) {
            this.state = region.state;
            this.roi = new Quad(region.roi);
            this.frameSize = new Size(region.frameSize.getWidth(), region.frameSize.getHeight());
            this.time = region.time;
            this.rotation = region.rotation;
        }
    }
    PageDetector() {
        mHandle = Create();
    }

    Region detect(byte[] frame, int width, int height, int rotation) {
        if(mReleased)
            throw new IllegalStateException("Detector has been released");
        ArrayList<Vec2> roiPoints = GetRoi(frame, width, height);
        Quad roi = new Quad(roiPoints);
        State state;
        if(roi == null || roiPoints.size() < 4) {
            state = State.NONE;
        } else if(distorted(roi)) {
            state = State.PERSPECTIVE;
        } else if(frameArea(width, height, roi) < 0.30) {
            state = State.SIZE;
        } else {
            state= State.LOCKED;
        }
        return new Region(state, roi, new Size(width, height), System.currentTimeMillis(), rotation);
    }

    void release() {
        if(!mReleased)
            Release();
        mReleased = true;
    }

    void thresholdImage(Bitmap input) {
        ThresholdImage(input);
    }

    private boolean distorted(Quad roi) {
        float distortion = GetDistortion(roi.getVecs());
        return distortion > 25;
    }

    private float frameArea(int width, int height, Quad roi) {
        float framePixelArea =width * height;
        float roiPixelArea =  GetArea(roi.getVecs());
        return roiPixelArea / framePixelArea;
    }
}
