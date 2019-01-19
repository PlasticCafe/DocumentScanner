package cafe.plastic.documentscanner.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.Nullable;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.util.Quad;
import cafe.plastic.documentscanner.vision.PageDetector;

public class FeatureOverlay extends View {
    private PageDetector.Region mCurrentRegion;
    private final Quad mDefaultPoints;
    private final Quad mPriorPoints;
    private final Quad mCurrentPoints;
    private int mDefaultSquareWidth;
    private Paint mStrokePaint;
    private Paint mFillPaint;
    private Paint mTextPaint;
    private ValueAnimator mCurrentAnimation;

    public FeatureOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        mPriorPoints = new Quad()
        mCurrentPoints = new ArrayList<>(Arrays.asList(new Point(), new Point(), new Point(), new Point()));
        mDefaultPoints = new ArrayList<>(Arrays.asList(new Point(), new Point(), new Point(), new Point()));
    }

    private void init(@Nullable AttributeSet attrs) {
        mFillPaint = new Paint();
        mStrokePaint = new Paint();
        mTextPaint = new Paint();
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setAntiAlias((true));
        if (attrs == null) return;
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.FeatureOverlay);
        mFillPaint.setColor(ta.getColor(R.styleable.FeatureOverlay_fill_color, Color.WHITE));
        mStrokePaint.setColor(ta.getColor(R.styleable.FeatureOverlay_stroke_color, Color.BLACK));
        float strokeWidth = ta.getDimensionPixelSize(R.styleable.FeatureOverlay_stroke_width, 1);
        mStrokePaint.setStrokeWidth(strokeWidth);
        mStrokePaint.setPathEffect(new DashPathEffect(new float[] {strokeWidth*3, strokeWidth}, 0));

        mTextPaint.setTypeface(Typeface.create("Arial", Typeface.NORMAL));
        mTextPaint.setTextSize(200);
        ta.recycle();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Path path = new Path();
        int centerx = 0;
        int centery = 0;
        path.moveTo(mCurrentPoints.get(0).x, mCurrentPoints.get(0).y);
        for (int i = 1; i <= 4; i++) {
            centerx += mCurrentPoints.get(i % 4).x;
            centery += mCurrentPoints.get(i % 4).y;
            path.lineTo(
                    mCurrentPoints.get(i % 4).x,
                    mCurrentPoints.get(i % 4).y);
        }
        path.close();
        centerx /= 4;
        centery /= 4;
        canvas.drawPath(path, mFillPaint);
        canvas.drawPath(path, mStrokePaint);
        mTextPaint.setTextSize(mDefaultSquareWidth/4);
        canvas.drawText("TestText", centerx, centery, mTextPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        mDefaultSquareWidth = (int)Math.min(width * 0.20f, height * 0.20f);
        int leftX = width/2 - mDefaultSquareWidth;
        int rightX = leftX + mDefaultSquareWidth*2;
        int topY = height/2 - mDefaultSquareWidth;
        int bottomY = topY + mDefaultSquareWidth*2;
        mDefaultPoints.get(0).set(rightX, topY);
        mDefaultPoints.get(1).set(rightX, bottomY);
        mDefaultPoints.get(2).set(leftX, bottomY);
        mDefaultPoints.get(3).set(leftX, topY);
    }

    public void updateRegion(PageDetector.Region region) {
        for (int i = 0; i < mCurrentPoints.size(); i++) {
            mPriorPoints.get(i).set(
                    mCurrentPoints.get(i).x,
                    mCurrentPoints.get(i).y);
        }
        mCurrentRegion = regionToScreen(region);
        if (mCurrentAnimation != null) mCurrentAnimation.cancel();
        mCurrentAnimation = ValueAnimator.ofFloat(0f, 1.0f).setDuration(100);
        mCurrentAnimation.addUpdateListener(valueAnimator -> {
            if (mCurrentRegion != null && mCurrentRegion.state != PageDetector.State.NONE) {
                for (int i = 0; i < mCurrentRegion.roi.size(); i++) {
                    Point p1 = mPriorPoints.get(i);
                    Point p2 = mCurrentRegion.roi.get(i);
                    lerp(p1, p2, mCurrentPoints.get(i), valueAnimator.getAnimatedFraction());
                }
            }
            else {
                for(int i = 0; i < mCurrentPoints.size(); i++) {
                    Point p1 = mPriorPoints.get(i);
                    Point p2 = mDefaultPoints.get(i);
                    lerp(p1, p2, mCurrentPoints.get(i), valueAnimator.getAnimatedFraction());
                }
            }
        });
        mCurrentAnimation.start();

        invalidate();

    }

    private PageDetector.Region regionToScreen(PageDetector.Region region) {
        ArrayList<Point> sourcePoints = region.roi;
        ArrayList<Point> screenPoints = new ArrayList<>();
        int xd, yd;
        for (Point point : sourcePoints) {
            switch (region.rotation) {
                case (90):
                    xd = point.y;
                    yd = region.frameSize.getWidth() - point.x;
                    break;
                case (180):
                    xd = region.frameSize.getWidth() - point.x;
                    yd = region.frameSize.getHeight() - point.y;
                    break;
                case (270):
                    xd = region.frameSize.getHeight() - point.y;
                    yd = point.x;
                    break;
                default:
                    xd = point.x;
                    yd = point.y;
                    break;
            }
            float scale = Math.max((float) this.getMeasuredWidth() / region.frameSize.getHeight(),
                    (float) this.getMeasuredHeight() / region.frameSize.getWidth());
            int width = (int) (region.frameSize.getHeight() * scale);
            int height = (int) (region.frameSize.getWidth() * scale);

            int excessX = Math.max(0, (width - this.getMeasuredWidth()) / 2);
            int excessY = Math.max(0, (height - this.getMeasuredHeight()) / 2);
            screenPoints.add(new Point((int) (xd * scale) - excessX, (int) (yd * scale) - excessY));
        }
        return new PageDetector.Region(region.state, screenPoints, region.frameSize, region.rotation);
    }

    private void lerp(Point p1, Point p2, Point output, float t) {
        int x1 = p1.x;
        int x2 = p2.x;
        int y1 = p1.y;
        int y2 = p2.y;

        int xt = (int) ((1.0f - t) * x1 + x2 * t);
        int yt = (int) ((1.0f - t) * y1 + y2 * t);
        output.set(xt, yt);
    }
}
