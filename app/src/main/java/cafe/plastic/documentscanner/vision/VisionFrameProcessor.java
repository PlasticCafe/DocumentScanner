package cafe.plastic.documentscanner.vision;

import android.graphics.Bitmap;
import android.graphics.Point;

import org.jetbrains.annotations.NotNull;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

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
    protected Flowable<Mat> mPreparedFrames;
    private Point mCurrentResolution;

    public VisionFrameProcessor() {
        mInput = PublishProcessor.<Frame>create();
        mFrames = mInput
                .onBackpressureLatest()
                .observeOn(Schedulers.computation());
        mResolution = PublishProcessor.<Point>create();
        mCurrentResolution = new Point(1, 1);
        mPreparedFrames = mFrames.map(f -> {
            Mat mCurrentMat = new Mat(f.getSize().height + f.getSize().height / 2, f.getSize().width, CvType.CV_8UC1);
            mCurrentMat.put(0, 0, f.getImage());
            Imgproc.cvtColor(mCurrentMat, mCurrentMat, Imgproc.COLOR_YUV2RGB_NV21);
            switch (f.getRotation()) {
                case 90:
                    Core.rotate(mCurrentMat, mCurrentMat, Core.ROTATE_90_COUNTERCLOCKWISE);
                    break;
                case 180:
                    Core.rotate(mCurrentMat, mCurrentMat, Core.ROTATE_180);
                    break;
                case 270:
                    Core.rotate(mCurrentMat, mCurrentMat, Core.ROTATE_90_CLOCKWISE);
                    break;
                default:
                    break;
            }
            if (mCurrentMat.width() != mCurrentResolution.x || mCurrentMat.height() != mCurrentResolution.y) {
                mCurrentResolution = new Point(mCurrentMat.width(), mCurrentMat.height());
                mResolution.onNext(mCurrentResolution);
            }
            return mCurrentMat;
        });
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
