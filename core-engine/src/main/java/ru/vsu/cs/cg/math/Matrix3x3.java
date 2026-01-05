package ru.vsu.cs.cg.math;

public class Matrix3x3 {
    private static final float EPSILON = 1e-7f;
    private float[][] data;

    /**
     * Конструктор по умолчанию.
     * Создает единичную матрицу (на главной диагонали 1, остальные 0).
     */
    public Matrix3x3() {
        data = new float[3][3];
        makeIdentity();
    }

    /**
     * Создает матрицу на основе переданного двумерного массива.
     *
     * @param values Массив 3x3 с значениями.
     * @throws IllegalArgumentException Если передан null или размер массива не 3x3.
     */
    public Matrix3x3(float[][] values) {
        if (values == null || values.length != 3 || values[0].length != 3) {
            throw new IllegalArgumentException("матрица должна быть 3x3");
        }
        data = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(values[i], 0, data[i], 0, 3);
        }
    }


    /**
     * Приводит текущую матрицу к единичному виду.
     * [1 0 0]
     * [0 1 0]
     * [0 0 1]
     */
    public void makeIdentity() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                data[i][j] = (i == j) ? 1 : 0;
            }
        }
    }

    /**
     * Заполняет матрицу нулями.
     */
    public void makeZero() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                data[i][j] = 0;
            }
        }
    }

    /**
     * Возвращает элемент матрицы по заданным индексам.
     *
     * @param row Индекс строки (0-2).
     * @param col Индекс столбца (0-2).
     * @return Значение элемента.
     * @throws IllegalArgumentException Если индексы выходят за пределы [0, 2].
     */
    public float get(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IllegalArgumentException("Нет индекса");
        }
        return data[row][col];
    }

    /**
     * Устанавливает значение элемента матрицы.
     *
     * @param row Индекс строки (0-2).
     * @param col Индекс столбца (0-2).
     * @param value Новое значение.
     * @throws IllegalArgumentException Если индексы выходят за пределы [0, 2].
     */
    public void set(int row, int col, float value) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IllegalArgumentException("нет индекса");
        }
        data[row][col] = value;
    }

    /**
     * Складывает текущую матрицу с другой матрицей.
     *
     * @param other Матрица, которую нужно прибавить.
     * @return Новая матрица, являющаяся результатом сложения.
     */
    public Matrix3x3 add(Matrix3x3 other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        return new Matrix3x3(result);
    }

    /**
     * Вычитает из текущей матрицы другую матрицу.
     *
     * @param other Матрица, которую нужно вычесть.
     * @return Новая матрица, являющаяся результатом вычитания.
     */
    public Matrix3x3 subtract(Matrix3x3 other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.data[i][j] - other.data[i][j];
            }
        }
        return new Matrix3x3(result);
    }

    /**
     * Умножает матрицу на вектор-столбец.
     *
     * @param vector Вектор для умножения.
     * @return Новый вектор, являющийся результатом умножения.
     */
    public Vector3f multiply(Vector3f vector) {
        float x = (data[0][0] * vector.getX() +
                data[0][1] * vector.getY() +
                data[0][2] * vector.getZ());

        float y = (data[1][0] * vector.getX() +
                data[1][1] * vector.getY() +
                data[1][2] * vector.getZ());

        float z = (data[2][0] * vector.getX() +
                data[2][1] * vector.getY() +
                data[2][2] * vector.getZ());

        return new Vector3f(x, y, z);
    }

    /**
     * Умножает текущую матрицу на другую матрицу.
     *
     * @param other Матрица, на которую нужно умножить (справа).
     * @return Новая матрица, являющаяся результатом умножения.
     */
    public Matrix3x3 multiply(Matrix3x3 other) {
        float[][] result = new float[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float sum = 0;
                for (int k = 0; k < 3; k++) {
                    sum += this.data[i][k] * other.data[k][j];
                }
                result[i][j] = sum;
            }
        }

        return new Matrix3x3(result);
    }

    /**
     * Транспонирует матрицу (строки становятся столбцами и наоборот).
     *
     * @return Новая транспонированная матрица.
     */
    public Matrix3x3 transpose() {
        float[][] result = new float[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.data[j][i];
            }
        }

        return new Matrix3x3(result);
    }

    /**
     * Проверяет равенство двух матриц с учетом погрешности EPSILON.
     *
     * @param obj Объект для сравнения.
     * @return true, если все элементы матриц совпадают с точностью до EPSILON.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix3x3 matrix3x3 = (Matrix3x3) obj;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (Math.abs(data[i][j] - matrix3x3.data[i][j]) > EPSILON) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matrix3:\n");
        for (int i = 0; i < 3; i++) {
            sb.append("[ ");
            for (int j = 0; j < 3; j++) {
                sb.append(String.format("%10.4f ", data[i][j]));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}