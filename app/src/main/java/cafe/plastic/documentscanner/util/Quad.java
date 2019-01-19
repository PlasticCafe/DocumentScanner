package cafe.plastic.documentscanner.util;

import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;

public class Quad {
    private final ArrayList<Vec2> mPoints = new ArrayList<>();

    public Quad() {
        for (int i = 0; i < 4; i++) mPoints.add(new Vec2());
    }

    public Quad(ArrayList<Vec2> inVecs) {
        this();
        if (inVecs.size() == 4) {
            for (int i = 0; i < 4; i++) {
                mPoints.get(i).set(inVecs.get(i));
            }
        }
    }

    public Quad(Quad inQuad) {
        this(inQuad.mPoints);
    }

    public Quad scale(float scalar) {
        ArrayList<Vec2> scaledVecs = new ArrayList<>();
        for (Vec2 vec : mPoints) {
            scaledVecs.add(vec.mul(scalar));
        }

        return new Quad(scaledVecs);
    }

    public Vec2 getCenter() {
        Vec2 center = new Vec2();
        for (Vec2 point : mPoints) {
            center.add(point);
        }
        return center.mul(0.25f);
    }

    public Vec2 getDimensions() {
        double width = Math.max(
                mPoints.get(3).sub(mPoints.get(0)).len(),
                mPoints.get(2).sub(mPoints.get(1)).len());
        double height = Math.max(
                mPoints.get(0).sub(mPoints.get(1)).len(),
                mPoints.get(3).sub(mPoints.get(2)).len());
        return new Vec2((float) width, (float) height);
    }

    public Quad lerp(Quad q2, float t) {
        ArrayList<Vec2> lerpedVecs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            lerpedVecs.add(mPoints.get(i).lerp(q2.mPoints.get(i), t));
        }
        return new Quad(lerpedVecs);
    }

    public Path toPath() {
        Path path = new Path();
        path.moveTo(mPoints.get(0).getX(), mPoints.get(0).getY());
        for (int i = 1; i <= 4; i++) {
            path.lineTo(
                    mPoints.get(i % 4).getX(),
                    mPoints.get(i % 4).getY());
        }
        path.close();
        return path;
    }

    public float[] toFloatArray() {
        float[] vecs = new float[8];
        for (int i = 0; i < 4; i++) {
            vecs[i * 2] = mPoints.get(i).getX();
            vecs[i * 2 + 1] = mPoints.get(i).getY();
        }
        return vecs;
    }
}
