package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.Point;

import org.jetbrains.annotations.NotNull;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;


public abstract class VisionFrameProcessor<T> implements FrameProcessor {
    protected PublishProcessor<Frame> mInput;
    protected Flowable<Frame> mFrames;
    private Point mCurrentResolution;

    public VisionFrameProcessor() {
        mInput = PublishProcessor.<Frame>create();
        mFrames = mInput
                .onBackpressureLatest()
                .observeOn(Schedulers.computation());
    }

    @Override
    public void process(@NotNull Frame frame) {
        mInput.onNext(frame);
    }

    public Flowable<Frame> getFrames() {
        return mFrames;
    }

    public abstract Flowable<T> processedOutput();

}
