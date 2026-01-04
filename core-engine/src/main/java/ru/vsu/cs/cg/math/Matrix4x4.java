package ru.vsu.cs.cg.math;

public class Matrix4x4 {
    private static final float EPSILON = 1e-7f;
    private float[][] data;


    public Matrix4x4() {
        data = new float[4][4];
        makeIdentity();
    }


    public Matrix4x4(float[][] values) {
        if (values == null || values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("матрица должна быть 4x4");
        }
        data = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, data[i], 0, 4);
        }
    }



    public void makeIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                data[i][j] = (i == j) ? 1 : 0;
            }
        }
    }


    public void makeZero() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                data[i][j] = 0;
            }
        }
    }


    public float get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Нет такого индекса");
        }
        return data[row][col];
    }


    public void set(int row, int col, float value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Нет такого индекса");
        }
        data[row][col] = value;
    }


    public Matrix4x4 add(Matrix4x4 other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        return new Matrix4x4(result);
    }


    public Matrix4x4 subtract(Matrix4x4 other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[i][j] - other.data[i][j];
            }
        }
        return new Matrix4x4(result);
    }


    public Vector4f multiply(Vector4f vector) {
        float x = data[0][0] * vector.getX() +
                data[0][1] * vector.getY() +
                data[0][2] * vector.getZ() +
                data[0][3] * vector.getW();

        float y = data[1][0] * vector.getX() +
                data[1][1] * vector.getY() +
                data[1][2] * vector.getZ() +
                data[1][3] * vector.getW();

        float z = data[2][0] * vector.getX() +
                data[2][1] * vector.getY() +
                data[2][2] * vector.getZ() +
                data[2][3] * vector.getW();

        float w = data[3][0] * vector.getX() +
                data[3][1] * vector.getY() +
                data[3][2] * vector.getZ() +
                data[3][3] * vector.getW();

        return new Vector4f(x, y, z, w);
    }


    public Matrix4x4 multiply(Matrix4x4 other) {
        float[][] result = new float[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += this.data[i][k] * other.data[k][j];
                }
                result[i][j] = sum;
            }
        }

        return new Matrix4x4(result);
    }


    public Matrix4x4 transpose() {
        float[][] result = new float[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[j][i];
            }
        }

        return new Matrix4x4(result);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix4x4 matrix4x4 = (Matrix4x4) obj;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(data[i][j] - matrix4x4.data[i][j]) > EPSILON) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matrix4:\n");
        for (int i = 0; i < 4; i++) {
            sb.append("[ ");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%10.4f ", data[i][j]));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}
