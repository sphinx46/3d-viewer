package ru.vsu.cs.cg.math;

public class Vector4f {
    private static final float EPSILON = 1e-7f;
    private float x;
    private float y;
    private float z;
    private float w;

    /**
     * Создает новый вектор с заданными координатами.
     *
     * @param x Координата X.
     * @param y Координата Y.
     * @param z Координата Z.
     * @param w Координата W.
     */
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

    /**
     * Складывает текущий вектор с другим вектором.
     *
     * @param other Вектор, который нужно прибавить.
     * @return Новый вектор, являющийся суммой.
     */
    public Vector4f add(Vector4f other) {
        return new Vector4f(
                this.x + other.x,
                this.y + other.y,
                this.z + other.z,
                this.w + other.w
        );
    }

    /**
     * Вычитает из текущего вектора другой вектор.
     *
     * @param other Вектор, который нужно вычесть.
     * @return Новый вектор, являющийся разностью.
     */
    public Vector4f subtract(Vector4f other) {
        return new Vector4f(
                this.x - other.x,
                this.y - other.y,
                this.z - other.z,
                this.w - other.w
        );
    }

    /**
     * Умножает вектор на скалярное значение.
     *
     * @param scalar Число, на которое умножается вектор.
     * @return Новый вектор, масштабированный на заданное значение.
     */
    public Vector4f multiply(float scalar) {
        return new Vector4f(
                this.x * scalar,
                this.y * scalar,
                this.z * scalar,
                this.w * scalar
        );
    }

    /**
     * Делит вектор на скалярное значение.
     *
     * @param scalar Число, на которое делится вектор.
     * @return Новый вектор, являющийся результатом деления.
     * @throws IllegalArgumentException Если скаляр равен нулю (меньше EPSILON).
     */
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

    /**
     * Вычисляет длину вектора.
     *
     * @return Длина вектора.
     */
    public float length() {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * Возвращает нормализованный вектор.
     *
     * @return Новый нормализованный вектор.
     * @throws IllegalArgumentException Если длина вектора равна нулю.
     */
    public Vector4f normalize() {
        float len = length();
        if (len < EPSILON) {
            throw new IllegalArgumentException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector4f(x / len, y / len, z / len, w / len);
    }

    /**
     * Безопасно возвращает нормализованный вектор.
     * Если длина вектора близка к нулю, возвращает копию текущего вектора.
     *
     * @return Новый нормализованный вектор или копия исходного, если длина равна 0.
     */
    public Vector4f normalizeSafe() {
        float len = length();
        if (len < EPSILON) {
            return new Vector4f(x, y, z, w);
        }
        return new Vector4f(x / len, y / len, z / len, w / len);
    }

    /**
     * Вычисляет скалярное произведение текущего вектора и другого вектора.
     *
     * @param other Вектор, с которым вычисляется произведение.
     * @return Значение скалярного произведения.
     */
    public float dot(Vector4f other) {
        return this.x * other.x + this.y * other.y +
                this.z * other.z + this.w * other.w;
    }

    /**
     * Преобразует вектор размерности 4 в вектор размерности 3, выполняя деление на W.
     * Координаты результата: (x/w, y/w, z/w).
     *
     * @return Новый Vector3f.
     * @throws IllegalArgumentException Если W близко к нулю.
     */
    public Vector3f toVector3() {
        if (Math.abs(w) < EPSILON) {
            throw new IllegalArgumentException("Невозможно преобразовать. Координата  w = 0");
        }
        return new Vector3f(x / w, y / w, z / w);
    }

    /**
     * Безопасно преобразует вектор размерности 4 в вектор размерности 3
     * @return  Если W не равно нулю, выполняет деление (x/w, y/w, z/w).
     * Если W близко к нулю, возвращает вектор (x, y, z) без деления.
     */
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
