package cafe.plastic.documentscanner.vision;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import io.fotoapparat.preview.Frame;
import io.fotoapparat.result.Photo;
import io.reactivex.Flowable;

public class ObjectTracker extends VisionFrameProcessor<PageDetector.Region> {
    private PageDetector mPageDetector = new PageDetector();

    @Override
    public Flowable<PageDetector.Region> processedOutput() {
        return mFrames.map(f -> processNative(f));
    }

    public void close() {
        mPageDetector.release();
    }

    private PageDetector.Region processNative(Frame frame) {
        return mPageDetector.detect(frame.getImage(), frame.getSize().width, frame.getSize().height, frame.getRotation());
    }
}
