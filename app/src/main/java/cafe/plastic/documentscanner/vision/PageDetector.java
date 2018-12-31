package cafe.plastic.documentscanner.vision;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.List;

import io.fotoapparat.preview.Frame;

public class PageDetector {
    private long mHandle;
    private boolean released = false;



    private native long Create();
    private native void Release();
    private native List<Point> GetRoi(byte[] frame, int width, int height, int rotation);
    private native float GetArea(List<Point> roi);
    private native float GetDistortion(List<Point> roi);
    public enum State {
        LOCKED,
        PERSPECTIVE,
        SIZE,
        NONE
    }

    public class Region {
        public State state;
        public List<Point> roi;

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

        public float[] toFloatArray() {
            float[] points = new float[roi.size()*2];
            for(int i = 0; i < roi.size(); i++) {
                points[i*2] = roi.get(i).x;
                points[i*2 + 1] = roi.get(i).y;
            }
            return points;
        }

        public void scale(double scale) {
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
    public PageDetector() {
        mHandle = Create();
    }

    public void initialize(Frame frame, Rect trackingRegion) {
        if(released)
            return;
    }

    public Region detect(byte[] frame, int width, int height, int rotation) {
        if(released)
            throw new IllegalStateException("Detector has been released");
        Region region = new Region();
        region.roi = GetRoi(frame, width, height, rotation);
        if(region.roi.size() != 4) {
            region.state = State.NONE;
        } else if(distorted(region.roi)) {
            region.state = State.PERSPECTIVE;
        } else if(frameArea(width, height, region.roi) < 0.30) {
            region.state = State.SIZE;
        } else {
            region.state= State.LOCKED;
        }
        return region;
    }


    public void release() {
        if(!released)
            Release();
        released = true;
    }

    private boolean distorted(List<Point> roi) {
        float distortion = GetDistortion(roi);
        if(distortion > 25) {
            return true;
        } else {
            return false;
        }
    }

    private float frameArea(int width, int height, List<Point> roi) {
        float framePixelArea =width * height;
        float roiPixelArea =  GetArea(roi);
        return roiPixelArea / framePixelArea;
    }
}
