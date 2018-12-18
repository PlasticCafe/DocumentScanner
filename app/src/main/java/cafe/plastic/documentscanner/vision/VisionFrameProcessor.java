package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.Point;

import org.jetbrains.annotations.NotNull;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static io.reactivex.processors.PublishProcessor.create;

public abstract class VisionFrameProcessor<T> implements FrameProcessor {
    protected PublishProcessor<Frame> mInput;
    protected PublishProcessor<Point> mResolution;
    protected Flowable<Frame> mFrames;
    private Point mCurrentResolution;

    public VisionFrameProcessor() {
        mInput = PublishProcessor.<Frame>create();
        mFrames = mInput
                .onBackpressureLatest()
                .observeOn(Schedulers.computation())
                .map(f -> {
                            if (f.getSize().width != mCurrentResolution.x || f.getSize().height != mCurrentResolution.y) {
                                mCurrentResolution = new Point(f.getSize().width, f.getSize().height);
                                mResolution.onNext(mCurrentResolution);
                            }
                            return f;
                        }
                );
        mResolution = PublishProcessor.<Point>create();
        mCurrentResolution = new Point(1, 1);
    }

    @Override
    public void process(@NotNull Frame frame) {
        mInput.onNext(frame);
    }

    public Point getFrameResolution() {
        return new Point(mCurrentResolution);
    }

    public Flowable<Point> currentFrameSize() {
        return mResolution.onBackpressureLatest();
    }

    public abstract Flowable<T> processedOutput();

}
