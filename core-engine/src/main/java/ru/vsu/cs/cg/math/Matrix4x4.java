package ru.vsu.cs.cg.math;

public class Matrix4x4 {
    private static final float EPSILON = 1e-7f;
    private float[][] data;

    /**
     * Конструктор по умолчанию.
     * Создает единичную матрицу.
     */
    public Matrix4x4() {
        data = new float[4][4];
        makeIdentity();
    }

    /**
     * Создает матрицу на основе переданного двумерного массива.
     *
     * @param values Массив 4x4 с значениями.
     * @throws IllegalArgumentException Если массив null или его размер не 4x4.
     */
    public Matrix4x4(float[][] values) {
        if (values == null || values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("матрица должна быть 4x4");
        }
        data = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, data[i], 0, 4);
        }
    }

    /**
     * Приводит текущую матрицу к единичному виду (Identity Matrix).
     * Элементы на главной диагонали равны 1, остальные 0.
     */
    public void makeIdentity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                data[i][j] = (i == j) ? 1 : 0;
            }
        }
    }


    /**
     * Заполняет матрицу нулями.
     */
    public void makeZero() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                data[i][j] = 0;
            }
        }
    }

    /**
     * Возвращает элемент матрицы по заданным индексам.
     *
     * @param row Индекс строки (0-3).
     * @param col Индекс столбца (0-3).
     * @return Значение элемента.
     * @throws IllegalArgumentException Если индексы выходят за пределы [0, 3].
     */
    public float get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Нет такого индекса");
        }
        return data[row][col];
    }

    /**
     * Устанавливает значение элемента матрицы.
     *
     * @param row Индекс строки (0-3).
     * @param col Индекс столбца (0-3).
     * @param value Новое значение.
     * @throws IllegalArgumentException Если индексы выходят за пределы [0, 3].
     */
    public void set(int row, int col, float value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IllegalArgumentException("Нет такого индекса");
        }
        data[row][col] = value;
    }

    /**
     * Складывает текущую матрицу с другой матрицей.
     *
     * @param other Матрица, которую нужно прибавить.
     * @return Новая матрица, являющаяся результатом сложения.
     */
    public Matrix4x4 add(Matrix4x4 other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        return new Matrix4x4(result);
    }

    /**
     * Вычитает из текущей матрицы другую матрицу.
     *
     * @param other Матрица, которую нужно вычесть.
     * @return Новая матрица, являющаяся результатом вычитания.
     */
    public Matrix4x4 subtract(Matrix4x4 other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[i][j] - other.data[i][j];
            }
        }
        return new Matrix4x4(result);
    }

    /**
     * Умножает матрицу на вектор-столбец (Vector4f).
     *
     * @param vector Вектор для умножения.
     * @return Новый трансформированный вектор.
     */
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

    /**
     * Умножает текущую матрицу на другую матрицу.
     *
     * @param other Матрица (правый операнд).
     * @return Новая матрица, являющаяся результатом умножения.
     */
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

    /**
     * Транспонирует матрицу (меняет строки и столбцы местами).
     *
     * @return Новая транспонированная матрица.
     */
    public Matrix4x4 transpose() {
        float[][] result = new float[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[j][i];
            }
        }

        return new Matrix4x4(result);
    }

    /**
     * Проверяет равенство двух матриц с учетом погрешности EPSILON.
     *
     * @param obj Объект для сравнения.
     * @return true, если все элементы равны (с точностью до EPSILON), иначе false.
     */
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

    public float determinant() {

        return data[0][0] * determinant3x3(1, 2, 3, 1, 2, 3) -
                data[0][1] * determinant3x3(1, 2, 3, 0, 2, 3) +
                data[0][2] * determinant3x3(1, 2, 3, 0, 1, 3) -
                data[0][3] * determinant3x3(1, 2, 3, 0, 1, 2);
    }

    /**
     * Вспомогательный метод для вычисления определителя матрицы 3x3.
     * Использует формулу Саррюса.
     *
     * @param r1, r2, r3 Индексы строк в исходной матрице 4x4
     * @param c1, c2, c3 Индексы столбцов в исходной матрице 4x4
     * @return Определитель матрицы 3x3
     */
    private float determinant3x3(int r1, int r2, int r3, int c1, int c2, int c3) {
        float a11 = data[r1][c1], a12 = data[r1][c2], a13 = data[r1][c3];
        float a21 = data[r2][c1], a22 = data[r2][c2], a23 = data[r2][c3];
        float a31 = data[r3][c1], a32 = data[r3][c2], a33 = data[r3][c3];

        return a11 * (a22 * a33 - a23 * a32) -
                a12 * (a21 * a33 - a23 * a31) +
                a13 * (a21 * a32 - a22 * a31);
    }

    /**
     * Вычисляет обратную матрицу.
     * Используется метод алгебраических дополнений (adjugate).
     * Для матрицы A: A⁻¹ = (1/det(A)) * adj(A)
     * где adj(A) - присоединенная матрица (транспонированная матрица алгебраических дополнений)
     *
     * @return Обратная матрица.
     * @throws ArithmeticException Если матрица вырожденная (определитель ≈ 0)
     */
    public Matrix4x4 inverse() {
        float det = determinant();

        if (Math.abs(det) < EPSILON) {
            throw new ArithmeticException("Matrix is singular (determinant = " + det + "). Cannot compute inverse.");
        }

        float invDet = 1.0f / det;

        float[][] cofactors = new float[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float minor = computeMinor(i, j);
                cofactors[i][j] = ((i + j) % 2 == 0 ? 1 : -1) * minor;
            }
        }

        float[][] adjugate = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                adjugate[j][i] = cofactors[i][j];
            }
        }

        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = adjugate[i][j] * invDet;
            }
        }

        return new Matrix4x4(result);
    }

    /**
     * Вычисляет минор матрицы (определитель подматрицы 3x3).
     *
     * @param row Строка для исключения
     * @param col Столбец для исключения
     * @return Определитель матрицы 3x3
     */
    private float computeMinor(int row, int col) {
        int[] rows = new int[3];
        int[] cols = new int[3];

        int rIdx = 0;
        for (int i = 0; i < 4; i++) {
            if (i != row) rows[rIdx++] = i;
        }

        int cIdx = 0;
        for (int j = 0; j < 4; j++) {
            if (j != col) cols[cIdx++] = j;
        }

        float a11 = data[rows[0]][cols[0]], a12 = data[rows[0]][cols[1]], a13 = data[rows[0]][cols[2]];
        float a21 = data[rows[1]][cols[0]], a22 = data[rows[1]][cols[1]], a23 = data[rows[1]][cols[2]];
        float a31 = data[rows[2]][cols[0]], a32 = data[rows[2]][cols[1]], a33 = data[rows[2]][cols[2]];

        return a11 * (a22 * a33 - a23 * a32) -
                a12 * (a21 * a33 - a23 * a31) +
                a13 * (a21 * a32 - a22 * a31);
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
