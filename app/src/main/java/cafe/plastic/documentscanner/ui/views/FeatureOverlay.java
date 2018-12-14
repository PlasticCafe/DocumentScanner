package cafe.plastic.documentscanner.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import androidx.annotation.Nullable;
import timber.log.Timber;

public class FeatureOverlay extends View {
    private List<Rect> mCurrentRects;
    private OnRegionSelectedListener mRegionSelectedCallback;
    private Point mCurrentResolution;
    private Paint mPointPaint;
    private Paint mBackgroundPaint;
    private Bitmap mCurrentBitmap;
    private Box mCurrentSelection = new Box();
    private boolean mSelecting = false;

    public FeatureOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCurrentResolution = new Point(1, 1);
        mPointPaint = new Paint();
        mPointPaint.setColor(0x22ff0000);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0x80f8efff);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Point coords = new Point((int) event.getX(), (int) event.getY());
        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                Timber.d("Action was DOWN");
                if (mSelecting)
                    break;
                else {
                    mCurrentSelection.setOrigin(coords);
                    mCurrentSelection.updateCurrent(coords);
                    mSelecting = true;
                    invalidate();
                    return true;
                }
            case (MotionEvent.ACTION_MOVE):
                if (mSelecting) {
                    mCurrentSelection.updateCurrent(coords);
                    invalidate();
                    return true;
                } else {
                    break;
                }
            case (MotionEvent.ACTION_UP):
                if (mSelecting) {
                    mCurrentSelection.updateCurrent(coords);
                    mSelecting = false;
                    if (mRegionSelectedCallback != null) {
                        mRegionSelectedCallback.onRegionSelected(calculateRectOnFrame(mCurrentSelection.toRect()));
                    }
                    invalidate();
                    return true;
                } else {
                    break;
                }
            case (MotionEvent.ACTION_CANCEL):
                Timber.d("Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                Timber.d("Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mCurrentRects != null) {
            for (Rect rect : mCurrentRects) {
                canvas.drawRect(calculateRectOnScreen(rect), mBackgroundPaint);
            }
        }
        if (mCurrentSelection != null) {
            canvas.drawRect(mCurrentSelection.toRect(), mPointPaint);
        }

        if (mCurrentBitmap != null) {
            canvas.drawBitmap(mCurrentBitmap, null, scaleOverlay(), mBackgroundPaint);
        }
    }

    public void updateRects(List<Rect> rects) {
        mCurrentRects = rects;
        invalidate();
    }

    public void updateBitmap(Bitmap bitmap) {
        mCurrentBitmap = bitmap;
        invalidate();
    }

    public void setOnRegionSelectedListener(OnRegionSelectedListener listener) {
        mRegionSelectedCallback = listener;
    }

    public void setFrameResolution(Point frame) {
        mCurrentResolution = new Point(frame);
    }

    public interface OnRegionSelectedListener {
        void onRegionSelected(Rect region);
    }


    private Rect calculateRectOnFrame(Rect screenRect) {
        float scale = Math.min((float) mCurrentResolution.x / this.getMeasuredWidth(),
                (float) mCurrentResolution.y / this.getMeasuredHeight());

        int width =  (int) (this.getMeasuredWidth() * scale);
        int height = (int) (this.getMeasuredHeight() * scale);

        int excessX = Math.max(0, (mCurrentResolution.x - width) / 2);
        int excessY = Math.max(0, (mCurrentResolution.y - height) / 2);
        Rect frameRect = new Rect(
                (int) (screenRect.left * scale) + excessX,
                (int) (screenRect.top * scale) + excessY,
                (int) (screenRect.right * scale) + excessX,
                (int) (screenRect.bottom * scale) + excessY);
        return frameRect;
    }

    private Rect calculateRectOnScreen(Rect frameRect) {
        float scale = Math.max((float) this.getMeasuredWidth()/ mCurrentResolution.x,
                (float)this.getMeasuredHeight() / mCurrentResolution.y);
        int width = (int) (mCurrentResolution.x * scale);
        int height = (int) (mCurrentResolution.y * scale);

        int excessX = Math.max(0, (width - this.getMeasuredWidth()) / 2);
        int excessY = Math.max(0, (height - this.getMeasuredHeight()) / 2);
        Rect screenRect = new Rect(
                (int) (frameRect.left * scale) - excessX,
                (int) (frameRect.top * scale) - excessY,
                (int) (frameRect.right * scale) - excessX,
                (int) (frameRect.bottom * scale) - excessY);
        return screenRect;
    }

    private Rect scaleOverlay() {
        float scale = Math.max(this.getMeasuredWidth() / (float) mCurrentResolution.x,
                this.getMeasuredHeight() / (float) mCurrentResolution.y);

        int width = (int) (scale * mCurrentResolution.x);
        int height = (int) (scale * mCurrentResolution.y);

        int excessX = Math.max(0, (width - this.getMeasuredWidth()) / 2);
        int excessY = Math.max(0, (height - this.getMeasuredHeight()) / 2);
        Rect targetRect = new Rect(-excessX,
                -excessY,
                width - excessX,
                height - excessY);
        return targetRect;
    }


    private class Box {
        Point mOrigin;
        Point mCurrent;

        public Box() {
            mOrigin = new Point();
            mCurrent = new Point();
        }

        public Box(int x1, int y1, int x2, int y2) {
            mOrigin = new Point(x1, y1);
            mCurrent = new Point(x2, y2);
        }

        public Box(Point point1, Point point2) {
            mOrigin = new Point(point1.x, point1.y);
            mCurrent = new Point(point2.x, point2.y);
        }

        public void setOrigin(Point origin) {
            mOrigin = origin;
        }

        public void setOrigin(int x, int y) {
            setOrigin(new Point(x, y));
        }

        public void updateCurrent(Point current) {
            mCurrent = current;
        }

        public void updateCurrent(int x, int y) {
            updateCurrent(new Point(x, y));
        }

        public Rect toRect() {
            return new Rect(
                    Math.min(mOrigin.x, mCurrent.x),
                    Math.min(mOrigin.y, mCurrent.y),
                    Math.max(mOrigin.x, mCurrent.x),
                    Math.max(mOrigin.y, mCurrent.y)
            );
        }
    }

}
