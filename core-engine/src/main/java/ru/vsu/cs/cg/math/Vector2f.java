package ru.vsu.cs.cg.math;

public class Vector2f {
    private static final float EPSILON = 1e-7f;
    private float x;
    private float y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }


    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }


    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }


    public Vector2f divide(float scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new IllegalArgumentException("нельзя делить на ноль");
        }
        return new Vector2f(this.x / scalar, this.y / scalar);
    }


    public float length() {
        return (float)Math.sqrt(x * x + y * y);
    }

    public Vector2f normalize() {
        float len = length();
        if (len < EPSILON) {
            throw new IllegalArgumentException("невозможно нормализовать нулевой вектор");
        }
        return new Vector2f(x / len, y / len);
    }

    public Vector2f normalizeSafe() {
        float len = length();
        if (len < EPSILON) {
            return new Vector2f(x, y);
        }
        return new Vector2f(x / len, y / len);
    }

    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    @Override
    public String toString() {
        return String.format("Vector2(%.4f, %.4f)", x, y);
    }
}