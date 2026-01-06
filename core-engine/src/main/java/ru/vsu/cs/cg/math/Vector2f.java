package ru.vsu.cs.cg.math;


public class Vector2f {
    public Vector2f() {
        this(0.0f, 0.0f);
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x;
    public float y;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
