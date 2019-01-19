package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

import cafe.plastic.documentscanner.util.Quad;

public class PageDetector {
    private boolean mReleased = false;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private long mHandle;


    private native long Create();
    private native void Release();
    private native Quad GetRoi(byte[] frame, int width, int height);
    private native float GetArea(Quad roi);
    private native float GetDistortion(Quad roi);
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
        public final int rotation;
        public Region(State state, Quad roi, Size frameSize, int rotation) {
            this.state = state;
            this.roi = roi;
            this.frameSize = frameSize;
            this.rotation = rotation;
        }
    }
    PageDetector() {
        mHandle = Create();
    }

    Region detect(byte[] frame, int width, int height, int rotation) {
        if(mReleased)
            throw new IllegalStateException("Detector has been mReleased");
        Quad roi = GetRoi(frame, width, height);
        State state;
        if(roi == null) {
            state = State.NONE;
            roi = new Quad();
        } else if(distorted(roi)) {
            state = State.PERSPECTIVE;
        } else if(frameArea(width, height, roi) < 0.30) {
            state = State.SIZE;
        } else {
            state= State.LOCKED;
        }
        return new Region(state, roi, new Size(width, height), rotation);
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
        float distortion = GetDistortion(roi);
        return distortion > 25;
    }

    private float frameArea(int width, int height, Quad roi) {
        float framePixelArea =width * height;
        float roiPixelArea =  GetArea(roi);
        return roiPixelArea / framePixelArea;
    }
}
