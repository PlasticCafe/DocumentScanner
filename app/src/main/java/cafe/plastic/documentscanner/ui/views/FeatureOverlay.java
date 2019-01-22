package cafe.plastic.documentscanner.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import androidx.annotation.Nullable;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.util.Quad;
import cafe.plastic.documentscanner.util.Vec2;
import cafe.plastic.documentscanner.vision.PageDetector;

public class FeatureOverlay extends View {
    private PageDetector.Region mLastestRegion;
    private final Quad mDefaultQuad;
    private final Quad mPriorQuad;
    private final Quad mCurrentQuad;
    private int mDefaultSquareWidth;
    private Paint mStrokePaint;
    private Paint mFillPaint;
    private Paint mTextPaint;
    private ValueAnimator mCurrentAnimation;

    public FeatureOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        mDefaultQuad = new Quad();
        mPriorQuad = new Quad();
        mCurrentQuad = new Quad();
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
        Path path = mCurrentQuad.toPath();
        Vec2 center = mCurrentQuad.getCenter();
        canvas.drawPath(path, mFillPaint);
        canvas.drawPath(path, mStrokePaint);
        mTextPaint.setTextSize(mDefaultSquareWidth/4.0f);
        canvas.drawText("TestText", center.getX(), center.getY(), mTextPaint);
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
        mDefaultQuad.set(rightX, topY, rightX, bottomY, leftX, bottomY, leftX, topY);
    }

    public void updateRegion(PageDetector.Region region) {
        mPriorQuad.set(mCurrentQuad);
        mLastestRegion = quadToScreen(region);
        if (mCurrentAnimation != null) mCurrentAnimation.cancel();
        mCurrentAnimation = ValueAnimator.ofFloat(0f, 1.0f).setDuration(100);
        mCurrentAnimation.addUpdateListener(valueAnimator -> {
            if (mLastestRegion.state != PageDetector.State.NONE) {
                mCurrentQuad.lerpInPlace(mPriorQuad, mLastestRegion.roi, valueAnimator.getAnimatedFraction());
            }
            else {
                mCurrentQuad.lerpInPlace(mPriorQuad, mDefaultQuad, valueAnimator.getAnimatedFraction());
            }
        });
        mCurrentAnimation.start();
        invalidate();

    }

    private PageDetector.Region quadToScreen(PageDetector.Region region) {
        ArrayList<Vec2> sourcePoints = region.roi.getVecs();
        ArrayList<Vec2> screenPoints = new ArrayList<>();
        float xd, yd;
        for (Vec2 vec : sourcePoints) {
            float x = vec.getX();
            float y = vec.getY();
            switch (region.rotation) {
                case (90):
                    xd = y;
                    yd = region.frameSize.getWidth() - x;
                    break;
                case (180):
                    xd = region.frameSize.getWidth() - x;
                    yd = region.frameSize.getHeight() - y;
                    break;
                case (270):
                    xd = region.frameSize.getHeight() - y;
                    yd = x;
                    break;
                default:
                    xd = x;
                    yd = y;
                    break;
            }
            float scale = Math.max((float) this.getMeasuredWidth() / region.frameSize.getHeight(),
                    (float) this.getMeasuredHeight() / region.frameSize.getWidth());
            int width = (int) (region.frameSize.getHeight() * scale);
            int height = (int) (region.frameSize.getWidth() * scale);

            int excessX = Math.max(0, (width - this.getMeasuredWidth()) / 2);
            int excessY = Math.max(0, (height - this.getMeasuredHeight()) / 2);
            screenPoints.add(new Vec2( xd * scale - excessX, yd * scale - excessY));
        }
        return new PageDetector.Region(region.state, new Quad(screenPoints), region.frameSize, region.rotation);
    }
}
