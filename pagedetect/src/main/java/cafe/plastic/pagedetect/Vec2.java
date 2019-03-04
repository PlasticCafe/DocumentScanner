package cafe.plastic.pagedetect;

public class Vec2 {
    private float x, y;

    public Vec2() {
        x = 0;
        y = 0;
    }

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(Vec2 srcVec) {
        this(srcVec.x, srcVec.y);
    }

    public Vec2 set(Vec2 srcVec) {
        return set(srcVec.x, srcVec.y);
    }

    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public double len() {
        return Math.sqrt(x * x + y * y);
    }

    public Vec2 add(Vec2 rhs) {
        return add(rhs.x, rhs.y);
    }

    public Vec2 add(float x, float y) {
        return new Vec2(this.x + x, this.y + y);
    }

    public Vec2 addInPlace(Vec2 rhs) {
        return addInPlace(rhs.x, rhs.y);
    }

    public Vec2 addInPlace(float x, float y) {
       this.x += x;
       this.y += y;
       return this;
    }

    public Vec2 sub(Vec2 rhs) {
        return new Vec2(x - rhs.x, y - rhs.y);
    }

    public Vec2 subInPlace(Vec2 rhs) {
        this.x -= rhs.x;
        this.y -= rhs.y;
        return this;
    }

    public Vec2 mul(float scalar) {
        return new Vec2(x * scalar, y * scalar);
    }

    public Vec2 mulInPlace(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public static Vec2 lerp(Vec2 v1, Vec2 v2, float t) {
        float xt =  (1.0f - t) * v1.x + v2.x * t;
        float yt =  (1.0f - t) * v1.y + v2.y * t;
        return new Vec2(xt, yt);
    }

    public Vec2 lerpInPlace(Vec2 v1, Vec2 v2, float t) {
        x = (1.0f - t) * v1.x + v2.x * t;
        y = (1.0f - t) * v1.y + v2.y * t;
        return this;
    }

}
