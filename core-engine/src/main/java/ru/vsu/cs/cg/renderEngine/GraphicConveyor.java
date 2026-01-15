package ru.vsu.cs.cg.renderEngine;

import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.math.Vector4f;

public class GraphicConveyor {

    /**
     * Создает матрицу переноса.
     *
     * @param tx Смещение по оси X.
     * @param ty Смещение по оси Y.
     * @param tz Смещение по оси Z.
     * @return Матрица 4x4, где значения смещения находятся в последнем столбце.
     */
    public static Matrix4x4 translate(float tx, float ty, float tz) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 3, tx);
        matrix.set(1, 3, ty);
        matrix.set(2, 3, tz);
        matrix.set(3, 3, 1);
        return matrix;
    }

    /**
     * Создает матрицу масштабирования.
     *
     * @param sx Коэффициент масштаба по оси X.
     * @param sy Коэффициент масштаба по оси Y.
     * @param sz Коэффициент масштаба по оси Z.
     * @return Диагональная матрица 4x4.
     */
    public static Matrix4x4 scale(float sx, float sy, float sz) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 0, sx);
        matrix.set(1, 1, sy);
        matrix.set(2, 2, sz);
        matrix.set(3, 3, 1);
        return matrix;
    }


    /**
     * Создает матрицу поворота вокруг оси X.
     *
     * @param angle Угол поворота.
     * @return Матрица поворота.
     */
    public static Matrix4x4 rotateX(float angle) {
        Matrix4x4 matrix = new Matrix4x4();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        matrix.set(0, 0, 1);
        matrix.set(1, 1, cos);
        matrix.set(1, 2, -sin);
        matrix.set(2, 1, sin);
        matrix.set(2, 2, cos);
        matrix.set(3, 3, 1);
        return matrix;
    }

    /**
     * Создает матрицу поворота вокруг оси Y.
     *
     * @param angle Угол поворота.
     * @return Матрица поворота.
     */
    public static Matrix4x4 rotateY(float angle) {
        Matrix4x4 matrix = new Matrix4x4();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        matrix.set(0, 0, cos);
        matrix.set(0, 2, sin);
        matrix.set(1, 1, 1);
        matrix.set(2, 0, -sin);
        matrix.set(2, 2, cos);
        matrix.set(3, 3, 1);
        return matrix;
    }

    /**
     * Создает матрицу поворота вокруг оси Z.
     *
     * @param angle Угол поворота.
     * @return Матрица поворота.
     */
    public static Matrix4x4 rotateZ(float angle) {
        Matrix4x4 matrix = new Matrix4x4();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        matrix.set(0, 0, cos);
        matrix.set(0, 1, -sin);
        matrix.set(1, 0, sin);
        matrix.set(1, 1, cos);
        matrix.set(2, 2, 1);
        matrix.set(3, 3, 1);
        return matrix;
    }

    /**
     * Собирает единую матрицу модели.
     * Переводит координаты вершины из локальных координат модели в мировые координаты.
     *  Итоговая формула: M = Translation * Rotation * Scale
     *
     * @param translation Вектор смещения.
     * @param rotation    Вектор углов поворота по осям X, Y, Z (в радианах).
     * @param scale       Вектор масштабирования.
     * @return Итоговая матрица модели (Model Matrix).
     */
    public static Matrix4x4 rotateScaleTranslate(Vector3f translation, Vector3f rotation, Vector3f scale) {
        Matrix4x4 scaleM = scale(scale.getX(), scale.getY(), scale.getZ());

        Matrix4x4 rotX = rotateX(rotation.getX());
        Matrix4x4 rotY = rotateY(rotation.getY());
        Matrix4x4 rotZ = rotateZ(rotation.getZ());

        Matrix4x4 rotationM = rotZ.multiply(rotY).multiply(rotX);

        Matrix4x4 translationM = translate(translation.getX(), translation.getY(), translation.getZ());

        return translationM.multiply(rotationM).multiply(scaleM);
    }

    /**
     * Создает матрицу вида на основе параметров камеры (LookAt).
     * Переводит координаты из мировых координат в пространство камеры.
     * В качестве вектора "Вверх" (Up) по умолчанию используется (0, 1, 0).
     *
     * @param eye    Позиция камеры (глаза наблюдателя).
     * @param target Точка, в которую смотрит камера.
     * @return Матрица вида.
     */
    public static Matrix4x4 lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }


    /**
     * Создает матрицу вида с произвольным вектором "Вверх".
     *
     * @param eye    Позиция камеры.
     * @param target Точка, в которую смотрит камера.
     * @param up     Вектор направления "Вверх" для камеры.
     * @return Матрица вида.
     */
    public static Matrix4x4 lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = target.subtract(eye);

        Vector3f resultX = up.cross(resultZ);

        Vector3f resultY = resultZ.cross(resultX);

        resultZ = resultZ.normalizeSafe();
        resultX = resultX.normalizeSafe();
        resultY = resultY.normalizeSafe();

        Matrix4x4 matrix = new Matrix4x4();

        matrix.set(0, 0, resultX.getX());
        matrix.set(0, 1, resultX.getY());
        matrix.set(0, 2, resultX.getZ());
        matrix.set(0, 3, -resultX.dot(eye));

        matrix.set(1, 0, resultY.getX());
        matrix.set(1, 1, resultY.getY());
        matrix.set(1, 2, resultY.getZ());
        matrix.set(1, 3, -resultY.dot(eye));

        matrix.set(2, 0, resultZ.getX());
        matrix.set(2, 1, resultZ.getY());
        matrix.set(2, 2, resultZ.getZ());
        matrix.set(2, 3, -resultZ.dot(eye));

        matrix.set(3, 3, 1);

        return matrix;
    }

    /**
     * Создает матрицу перспективной проекции.
     * Переводит координаты из пространства камеры в пространство отсечения.
     * Определяет форму усеченной пирамиды видимости.
     *
     * @param fov         Угол обзора по вертикали.
     * @param aspectRatio Соотношение сторон экрана (ширина / высота).
     * @param nearPlane   Расстояние до ближней плоскости отсечения.
     * @param farPlane    Расстояние до дальней плоскости отсечения.
     * @return Матрица проекции.
     */
    public static Matrix4x4 perspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.makeZero();

        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));

        matrix.set(0, 0, tangentMinusOnDegree / aspectRatio);
        matrix.set(1, 1, tangentMinusOnDegree);

        matrix.set(2, 2, (farPlane + nearPlane) / (farPlane - nearPlane));

        matrix.set(2, 3, (2 * nearPlane * farPlane) / (nearPlane - farPlane));

        matrix.set(3, 2, 1.0F);

        return matrix;
    }

    /**
     * Преобразует нормализованные координаты устройства (NDC) в экранные координаты (пиксели).
     * NDC координаты находятся в диапазоне [-1, 1].
     * Экранные координаты находятся в диапазоне [0, width] и [0, height].
     *
     * @param vertex Нормализованные координаты (x, y от -1 до 1).
     * @param width  Ширина экрана.
     * @param height Высота экрана.
     * @return Вектор 2f с координатами пикселя (x, y).
     */
    public static Vector2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Vector2f(
                (vertex.getX() + 1.0F) * width / 2.0F,
                (1.0F - vertex.getY()) * height / 2.0F
        );
    }

    /**
     * Умножает матрицу 4x4 на 3D-вектор, интерпретируя его как точку в пространстве.
     * Исходный вектор дополняется четвертой компонентой w = 1.0 (переход в однородные координаты).
     * Это позволяет матрице применять к точке операции переноса (translation).
     * Результат возвращается в "сыром" виде вектора размерности 4, сохраняя компоненту w,
     * что необходимо для дальнейших операций (например, клиппинга или отложенного перспективного деления).
     *
     * @param matrix Матрица преобразования.
     * @param vertex Исходный 3D-вектор (точка).
     * @return Преобразованный вектор в виде вектора размерности 4 (в однородных координатах).
     */
    public static Vector4f multiplyMatrix4ByVector3ToVector4(Matrix4x4 matrix, Vector3f vertex) {
        return matrix.multiply(new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1.0F));
    }

    /**
     * Преобразует вектор нормали (3D) с помощью матрицы 4x4.
     * Используется для преобразования нормалей из локальных координат модели в мировые координаты.
     *
     * @param matrix Матрица преобразования (должна быть обратной транспонированной для правильного преобразования нормалей)
     * @param normal Исходный вектор нормали в локальных координатах модели
     * @return Преобразованный вектор нормали в мировых координатах
     */
    public static Vector3f multiplyMatrix4ByVector3Normal(Matrix4x4 matrix, Vector3f normal) {
        Vector4f normal4 = new Vector4f(normal.getX(), normal.getY(), normal.getZ(), 0.0F);
        Vector4f result4 = matrix.multiply(normal4);
        return new Vector3f(result4.getX(), result4.getY(), result4.getZ());
    }

    /**
     * Применяет матричное преобразование к трехмерному вектору.
     * Выполняет следующие шаги:
     *
     * @param matrix Матрица преобразования.
     * @param vertex Исходный вектор.
     * @return Преобразованный вектор в нормализованных координатах (если была применена проекция).
     */
    public static Vector3f multiplyMatrix4ByVector3(Matrix4x4 matrix, Vector3f vertex) {
        Vector4f vertex4 = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1.0F);
        Vector4f result4 = matrix.multiply(vertex4);
        return result4.toVector3Safe();
    }

}