package ru.vsu.cs.cg.math;

public class Vector4f {
    private static final float EPSILON = 1e-4f;
    private float x;
    private float y;
    private float z;
    private float w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }


    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setW(float w) {
        this.w = w;
    }


    public Vector4f add(Vector4f other) {
        return new Vector4f(
                this.x + other.x,
                this.y + other.y,
                this.z + other.z,
                this.w + other.w
        );
    }


    public Vector4f subtract(Vector4f other) {
        return new Vector4f(
                this.x - other.x,
                this.y - other.y,
                this.z - other.z,
                this.w - other.w
        );
    }

    public Vector4f multiply(float scalar) {
        return new Vector4f(
                this.x * scalar,
                this.y * scalar,
                this.z * scalar,
                this.w * scalar
        );
    }


    public Vector4f divide(float scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new IllegalArgumentException("Деление на ноль");
        }
        return new Vector4f(
                this.x / scalar,
                this.y / scalar,
                this.z / scalar,
                this.w / scalar
        );
    }

    public float length() {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w);
    }


    public Vector4f normalize() {
        float len = length();
        if (len < EPSILON) {
            throw new IllegalArgumentException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector4f(x / len, y / len, z / len, w / len);
    }

    public Vector4f normalizeSafe() {
        float len = length();
        if (len < EPSILON) {
            return new Vector4f(x, y, z, w);
        }
        return new Vector4f(x / len, y / len, z / len, w / len);
    }

    public float dot(Vector4f other) {
        return this.x * other.x + this.y * other.y +
                this.z * other.z + this.w * other.w;
    }

    public Vector3f toVector3() {
        if (Math.abs(w) < EPSILON) {
            throw new IllegalArgumentException("Невозможно преобразовать. Координата  w = 0");
        }
        return new Vector3f(x / w, y / w, z / w);
    }

    public Vector3f toVector3Safe() {
        if (Math.abs(w) < EPSILON) {
            return new Vector3f(x, y, z);
        }
        return new Vector3f(x / w, y / w, z / w);
    }

    @Override
    public String toString() {
        return String.format("Vector4(%.4f, %.4f, %.4f, %.4f)", x, y, z, w);
    }
}
