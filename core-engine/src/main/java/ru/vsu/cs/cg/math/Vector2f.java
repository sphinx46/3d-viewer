package ru.vsu.cs.cg.math;

public class Vector2f {
    private static final float EPSILON = 1e-7f;
    private float x;
    private float y;

    /**
     * Создает новый вектор с заданными координатами.
     *
     * @param x Координата по оси X.
     * @param y Координата по оси Y.
     */
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

    /**
     * Складывает текущий вектор с другим вектором.
     *
     * @param other Вектор, который нужно прибавить.
     * @return Новый вектор, являющийся результатом сложения.
     */
    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    /**
     * Вычитает из текущего вектора другой вектор.
     *
     * @param other Вектор, который нужно вычесть.
     * @return Новый вектор, являющийся результатом вычитания.
     */
    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    /**
     * Умножает вектор на скалярное значение.
     *
     * @param scalar Число, на которое умножается вектор.
     * @return Новый вектор, являющийся результатом умножения.
     */
    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    /**
     * Делит вектор на скалярное значение.
     *
     * @param scalar Число, на которое делится вектор.
     * @return Новый вектор, являющийся результатом деления.
     * @throws IllegalArgumentException Если переданный скаляр равен нулю (меньше EPSILON).
     */
    public Vector2f divide(float scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new IllegalArgumentException("нельзя делить на ноль");
        }
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    /**
     * Вычисляет длину (модуль) вектора.
     *
     * @return Длина вектора.
     */
    public float length() {
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * Возвращает нормализованный вектор (единичный вектор того же направления).
     *
     * @return Новый нормализованный вектор.
     * @throws IllegalArgumentException Если длина вектора равна нулю (меньше EPSILON).
     */
    public Vector2f normalize() {
        float len = length();
        if (len < EPSILON) {
            throw new IllegalArgumentException("невозможно нормализовать нулевой вектор");
        }
        return new Vector2f(x / len, y / len);
    }

    /**
     * Безопасно возвращает нормализованный вектор.
     * Если длина вектора близка к нулю, возвращает копию исходного вектора (без изменений),
     * вместо выбрасывания исключения.
     *
     * @return Новый нормализованный вектор или копия исходного, если длина равна 0.
     */
    public Vector2f normalizeSafe() {
        float len = length();
        if (len < EPSILON) {
            return new Vector2f(x, y);
        }
        return new Vector2f(x / len, y / len);
    }

    /**
     * Вычисляет скалярное произведение текущего вектора и другого вектора.
     *
     * @param other Вектор, с которым вычисляется произведение.
     * @return Значение скалярного произведения.
     */
    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    @Override
    public String toString() {
        return String.format("Vector2(%.4f, %.4f)", x, y);
    }
}