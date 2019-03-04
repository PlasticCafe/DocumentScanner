package cafe.plastic.pagedetect;

import android.graphics.Path;

import java.util.ArrayList;

public class Quad {
    private final ArrayList<Vec2> mPoints = new ArrayList<>();

    public Quad() {
        for (int i = 0; i < 4; i++) mPoints.add(new Vec2());
    }

    public Quad(ArrayList<Vec2> inVecs) {
        this();
        this.set(inVecs);
    }

    public Quad(Quad inQuad) {
        this(inQuad.mPoints);
    }

    public Quad copy() {
        return new Quad(this);
    }

    public Quad scale(float scalar) {
        for(Vec2 vec : mPoints) {
            vec.mulInPlace(scalar);
        }
        return this;
    }

    public ArrayList<Vec2> getVecs() {
        ArrayList<Vec2> copy = new ArrayList<>();
        for(Vec2 vec: mPoints) {
            copy.add(new Vec2(vec));
        }
        return copy;
    }

    public Vec2 getCenter() {
        Vec2 center = new Vec2();
        for (Vec2 point : mPoints) {
            center.addInPlace(point);
        }
        return center.mulInPlace(0.25f);
    }

    public Vec2 getDimensions() {
        double width = Math.max(
                mPoints.get(0).sub(mPoints.get(1)).len(),
                mPoints.get(3).sub(mPoints.get(2)).len());
        double height = Math.max(
                mPoints.get(0).sub(mPoints.get(3)).len(),
                mPoints.get(1).sub(mPoints.get(2)).len());
        return new Vec2((float) width, (float) height);
    }

    public Quad lerp(Quad q1, Quad q2, float t) {
        for(int i = 0; i < 4; i++) {
            mPoints.get(i).lerpInPlace(q1.mPoints.get(i), q2.mPoints.get(i), t);
        }
        return this;
    }

    public Quad translate(float x, float y) {
        for(Vec2 vec: mPoints) {
            vec.addInPlace(x, y);
        }
        return this;
    }

    public void set(Quad inQuad) {
        set(inQuad.mPoints);
    }

    public void set(ArrayList<Vec2> inVecs) {
        if(inVecs.size() == 4) {
            for (int i = 0; i < 4; i++) {
                mPoints.get(i).set(inVecs.get(i));
            }
        }
    }

    public void set(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        mPoints.get(0).set(x1, y1);
        mPoints.get(1).set(x2, y2);
        mPoints.get(2).set(x3, y3);
        mPoints.get(3).set(x4, y4);
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
