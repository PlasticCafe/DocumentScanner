package cafe.plastic.documentscanner.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.renderscript.Sampler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import timber.log.Timber;

public class CropRotateView extends View {
    private Bitmap bitmap;
    private Float rotation = 0.0f;
    private Float animatedRotationPosition = rotation;
    private Matrix bitmapMatrix;
    private Matrix diagMatrix;
    private Paint paint;
    private ValueAnimator spinAnimation;

    public CropRotateView(Context context) {
        super(context);
        init();
    }

    public CropRotateView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        bitmapMatrix = new Matrix();
        diagMatrix = new Matrix();
        paint = new Paint();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    public void setRotation(float rotation) {
        Timber.d("Setting rotation from %f to %f", this.rotation, rotation);

        if (spinAnimation != null)
            spinAnimation.cancel();
        rotation = rotation % 360;
        if (rotation < this.rotation)
            spinAnimation = ValueAnimator.ofFloat(this.rotation, rotation + 360.0f);
        else
            spinAnimation = ValueAnimator.ofFloat(this.rotation, rotation);
        spinAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CropRotateView.this.rotation = (float)animation.getAnimatedValue() % 360.0f;
                invalidate();
                Timber.d("Animation tick");
            }
        });
        spinAnimation.start();
    }

    private float getScalingFactor() {
        float[] diagonals = new float[]{
                0.0f, 0.0f,
                bitmap.getWidth(), bitmap.getHeight(),
                bitmap.getWidth(), 0.0f,
                0.0f, bitmap.getHeight()};
        diagMatrix.reset();
        diagMatrix.postRotate(rotation);
        diagMatrix.mapPoints(diagonals);
        float rotatedWidth = Math.max(Math.abs(diagonals[0] - diagonals[2]), Math.abs(diagonals[4] - diagonals[6]));
        float rotatedHeight = Math.max(Math.abs(diagonals[1] - diagonals[3]), Math.abs(diagonals[5] - diagonals[7]));
        float scaleX = getWidth() / rotatedWidth;
        float scaleY = getHeight() / rotatedHeight;
        return scaleX < scaleY ? scaleX : scaleY;
    }

    private void drawBitmap(Canvas canvas) {
        if (bitmap != null) {
                Timber.d("Drawing rotation animation");
                float scale = getScalingFactor();
                float scaledWidth = scale * bitmap.getWidth();
                float scaledHeight = scale * bitmap.getHeight();
                bitmapMatrix.reset();
                bitmapMatrix.postRotate(rotation, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
                bitmapMatrix.postScale(scale, scale);
                bitmapMatrix.postTranslate((getWidth() / 2.0f) - scaledWidth / 2.0f, (getHeight() / 2.0f) - scaledHeight / 2.0f);
                canvas.drawBitmap(bitmap, bitmapMatrix, paint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmap(canvas);
    }
}
