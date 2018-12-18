package cafe.plastic.documentscanner.vision;

import android.graphics.Rect;

import java.util.List;

import io.fotoapparat.preview.Frame;

public class PageDetector {
    private long mHandle;
    private boolean released = false;
    private native long Create();
    private native void Release();
    private native void Initialize(byte[] frame, int width, int height, int left, int top, int right, int bottom, int rotation);
    private native List<Rect> Detect(byte[] frame, int width, int height, int rotation);

    public PageDetector() {
        mHandle = Create();
    }

    public void initialize(Frame frame, Rect trackingRegion) {
        if(released)
            return;
        Initialize(
                frame.getImage(),
                frame.getSize().width,
                frame.getSize().height,
                frame.getRotation(),
                trackingRegion.left,
                trackingRegion.top,
                trackingRegion.right,
                trackingRegion.bottom
                );
    }

    public List<Rect> detect(Frame frame) {
        if(released)
            throw new IllegalStateException("Detector has been released");
        return Detect(frame.getImage(), frame.getSize().width, frame.getSize().height, frame.getRotation());
    }

    public void release() {
        if(!released)
            Release();
        released = true;
    }
}
