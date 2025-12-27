package ru.vsu.cs.cg.math;

public class Vector3f {
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Vector3f other) {
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) <
                eps && Math.abs(z - other.z) < eps;
    }

    public float x;
    public float y;
    public float z;
}
