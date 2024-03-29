package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

public class PageDetector {
    private boolean mReleased = false;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private long mHandle;


    private native long Create();
    private native void Release();
    private native ArrayList<Point> GetRoi(byte[] frame, int width, int height);
    private native float GetArea(List<Point> roi);
    private native float GetDistortion(List<Point> roi);
    private native void ThresholdImage(Bitmap input);
    public enum State {
        LOCKED,
        PERSPECTIVE,
        SIZE,
        NONE
    }

    public static class Region {
        public final State state;
        public final ArrayList<Point> roi;
        public final Size frameSize;
        public final int rotation;
        public Region(State state, ArrayList<Point> roi, Size frameSize, int rotation) {
            this.state = state;
            this.roi = roi;
            this.frameSize = frameSize;
            this.rotation = rotation;
        }

        public Point getDimensions() {
            if(roi.size() != 4) return new Point(0, 0);
            Point vec1 = sub(roi.get(0), roi.get(1));
            Point vec2 = sub(roi.get(3), roi.get(2));
            Point vec3 = sub(roi.get(0), roi.get(3));
            Point vec4 = sub(roi.get(1), roi.get(2));
            int width = (int)Math.max(norm(vec1), norm(vec2));
            int height = (int)Math.max(norm(vec3), norm(vec4));
            return new Point(width, height);

        }

        float[] toFloatArray() {
            float[] points = new float[roi.size()*2];
            for(int i = 0; i < roi.size(); i++) {
                points[i*2] = roi.get(i).x;
                points[i*2 + 1] = roi.get(i).y;
            }
            return points;
        }

        void scale(double scale) {
            for(int i = 0; i < roi.size(); i++) {
                roi.get(i).x *= scale;
                roi.get(i).y *= scale;
            }
        }

        private Point sub(Point p1, Point p2) {
            return new Point (p1.x - p2.x, p1.y - p2.y);
        }

        private double norm(Point p1) {
           return Math.sqrt(p1.x*p1.x + p1.y*p1.y);
        }
    }
    PageDetector() {
        mHandle = Create();
    }

    Region detect(byte[] frame, int width, int height, int rotation) {
        if(mReleased)
            throw new IllegalStateException("Detector has been mReleased");
        ArrayList<Point> roi = GetRoi(frame, width, height);
        State state;
        if(roi.size() != 4) {
            state = State.NONE;
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

    private boolean distorted(List<Point> roi) {
        float distortion = GetDistortion(roi);
        return distortion > 25;
    }

    private float frameArea(int width, int height, List<Point> roi) {
        float framePixelArea =width * height;
        float roiPixelArea =  GetArea(roi);
        return roiPixelArea / framePixelArea;
    }
}
