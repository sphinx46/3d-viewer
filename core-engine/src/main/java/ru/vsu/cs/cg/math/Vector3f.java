package ru.vsu.cs.cg.math;

public class Vector3f {
    private static final float EPSILON = 1e-7f;
    private final float x;
    private final float y;
    private final float z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
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

    public Vector3f add(Vector3f other) {
        return new Vector3f(
                this.x + other.x,
                this.y + other.y,
                this.z + other.z
        );
    }

    public Vector3f subtract(Vector3f other) {
        return new Vector3f(
                this.x - other.x,
                this.y - other.y,
                this.z - other.z
        );
    }

    public Vector3f multiply(float scalar) {
        return new Vector3f(
                this.x * scalar,
                this.y * scalar,
                this.z * scalar
        );
    }

    public Vector3f multiply(Vector3f other) {
        return new Vector3f(
                x * other.x,
                y * other.y,
                z * other.z
        );
    }

    public Vector3f divide(float scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new IllegalArgumentException("деление на ноль");
        }

        float invLen = 1.0f / scalar;
        return new Vector3f(x * invLen, y * invLen, z * invLen);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3f normalized() {
        float len = length();
        if (len < EPSILON) {
            throw new IllegalArgumentException("Невозможно нормализовать нулевой вектор");
        }

        float invLen = 1.0f / len;
        return new Vector3f(x * invLen, y * invLen, z * invLen);
    }

    public Vector3f normalizeSafe() {
        float len = length();
        if (len < EPSILON) {
            return this;
        }

        float invLen = 1.0f / len;
        return new Vector3f(x * invLen, y * invLen, z * invLen);
    }

    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%.4f, %.4f, %.4f)", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Vector3f other = (Vector3f) obj;
        return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON &&
                Math.abs(z - other.z) < EPSILON;
    }
}
