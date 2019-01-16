package cafe.plastic.documentscanner.vision;

import org.jetbrains.annotations.NotNull;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;


abstract class VisionFrameProcessor<T> implements FrameProcessor {
    private final PublishProcessor<Frame> mInput;
    final Flowable<Frame> mFrames;

    VisionFrameProcessor() {
        mInput = PublishProcessor.create();
        mFrames = mInput
                .onBackpressureLatest()
                .observeOn(Schedulers.computation());
    }

    @Override
    public void process(@NotNull Frame frame) {
        mInput.onNext(frame);
    }


}
