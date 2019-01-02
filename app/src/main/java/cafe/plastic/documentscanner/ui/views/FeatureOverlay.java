package cafe.plastic.documentscanner.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import cafe.plastic.documentscanner.vision.PageDetector;

public class FeatureOverlay extends View {
    private PageDetector.Region mCurrentRegion;
    private Paint mPointPaint;

    public FeatureOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPointPaint = new Paint();
        mPointPaint.setColor(0x22ff0000);
        mPointPaint.setStrokeWidth(5.0f);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mCurrentRegion != null) {
            ArrayList<Point> roi = mCurrentRegion.roi;
            if (roi != null && roi.size() == 4) {
                Path path = new Path();
                path.moveTo(roi.get(0).x, roi.get(0).y);
                for (int i = 1; i <= roi.size(); i++) {
                    path.lineTo(
                            roi.get(i % roi.size()).x,
                            roi.get(i % roi.size()).y);
                }
                path.close();
                canvas.drawPath(path, mPointPaint);
            }
        }
    }

    public void updateRegion(PageDetector.Region region) {
        mCurrentRegion = regionToScreen(region);
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
}
