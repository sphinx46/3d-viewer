package ru.vsu.cs.cg.render_engine;

import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;

public class GraphicConveyor {

    public static Matrix4x4 translate(float tx, float ty, float tz) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 3, tx);
        matrix.set(1, 3, ty);
        matrix.set(2, 3, tz);
        return matrix;
    }

    public static Matrix4x4 scale(float sx, float sy, float sz) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.set(0, 0, sx);
        matrix.set(1, 1, sy);
        matrix.set(2, 2, sz);
        return matrix;
    }

    public static Matrix4x4 rotateX(float angle) {
        Matrix4x4 matrix = new Matrix4x4();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        matrix.set(1, 1, cos);
        matrix.set(1, 2, -sin);
        matrix.set(2, 1, sin);
        matrix.set(2, 2, cos);
        return matrix;
    }

    public static Matrix4x4 rotateY(float angle) {
        Matrix4x4 matrix = new Matrix4x4();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        matrix.set(0, 0, cos);
        matrix.set(0, 2, sin);
        matrix.set(2, 0, -sin);
        matrix.set(2, 2, cos);
        return matrix;
    }

    public static Matrix4x4 rotateZ(float angle) {
        Matrix4x4 matrix = new Matrix4x4();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        matrix.set(0, 0, cos);
        matrix.set(0, 1, -sin);
        matrix.set(1, 0, sin);
        matrix.set(1, 1, cos);
        return matrix;
    }

    public static Matrix4x4 rotateScaleTranslate(Vector3f translation, Vector3f rotation, Vector3f scale) {
        Matrix4x4 scaleM = scale(scale.getX(), scale.getY(), scale.getZ());

        Matrix4x4 rotX = rotateX(rotation.getX());
        Matrix4x4 rotY = rotateY(rotation.getY());
        Matrix4x4 rotZ = rotateZ(rotation.getZ());

        Matrix4x4 rotationM = rotZ.multiply(rotY).multiply(rotX);

        Matrix4x4 translationM = translate(translation.getX(), translation.getY(), translation.getZ());

        return translationM.multiply(rotationM).multiply(scaleM);
    }

    public static Matrix4x4 lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4x4 lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = target.subtract(eye);

        Vector3f resultX = resultZ.cross(up);

        Vector3f resultY = resultX.cross(resultZ);

        resultZ = resultZ.normalizeSafe();
        resultX = resultX.normalizeSafe();
        resultY = resultY.normalizeSafe();

        resultY = resultY.multiply(-1);

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

    public static Matrix4x4 perspective(float fov, float aspectRatio, float nearPlane, float farPlane) {
        Matrix4x4 matrix = new Matrix4x4();
        matrix.makeZero();

        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));

        matrix.set(0, 0, tangentMinusOnDegree);
        matrix.set(1, 1, tangentMinusOnDegree / aspectRatio);
        matrix.set(2, 2, (farPlane + nearPlane) / (farPlane - nearPlane));
        matrix.set(2, 3, (2 * nearPlane * farPlane) / (nearPlane - farPlane));
        matrix.set(3, 2, 1.0F);

        return matrix;
    }

    public static Vector2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Vector2f(
                vertex.getX() * width + width / 2.0F,
                -vertex.getY() * height + height / 2.0F
        );
    }
}