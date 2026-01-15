package ru.vsu.cs.cg.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ru.vsu.cs.cg.renderEngine.GraphicConveyor;

public class AffineTransformationsTest {

    private static final float EPSILON = 1e-5f;

    @Test
    public void testTranslation() {
        Vector3f original = new Vector3f(1, 2, 3);

        Matrix4x4 translationMatrix = GraphicConveyor.translate(10, -5, 0);

        Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(translationMatrix, original);

        Assertions.assertEquals(11.0f, result.getX(), EPSILON);
        Assertions.assertEquals(-3.0f, result.getY(), EPSILON);
        Assertions.assertEquals(3.0f,  result.getZ(), EPSILON);
    }

    @Test
    public void testScaling() {
        Vector3f original = new Vector3f(2, 4, 8);

        Matrix4x4 scaleMatrix = GraphicConveyor.scale(0.5f, 2.0f, 1.0f);

        Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(scaleMatrix, original);

        Assertions.assertEquals(1.0f, result.getX(), EPSILON);
        Assertions.assertEquals(8.0f, result.getY(), EPSILON);
        Assertions.assertEquals(8.0f, result.getZ(), EPSILON);
    }

    @Test
    public void testRotationX() {
        Vector3f original = new Vector3f(0, 1, 0);

        float angle = (float) Math.toRadians(90);
        Matrix4x4 rotateXMatrix = GraphicConveyor.rotateX(angle);

        Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(rotateXMatrix, original);
        Assertions.assertEquals(0.0f, result.getX(), EPSILON);
        Assertions.assertEquals(0.0f, result.getY(), EPSILON);
        Assertions.assertEquals(1.0f, result.getZ(), EPSILON);
    }

    @Test
    public void testRotationZ() {
        Vector3f original = new Vector3f(1, 0, 0);

        float angle = (float) Math.toRadians(90);
        Matrix4x4 rotateZMatrix = GraphicConveyor.rotateZ(angle);

        Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(rotateZMatrix, original);

        Assertions.assertEquals(0.0f, result.getX(), EPSILON);
        Assertions.assertEquals(1.0f, result.getY(), EPSILON);
        Assertions.assertEquals(0.0f, result.getZ(), EPSILON);
    }

    @Test
    public void testComplexTransformationOrder() {
        Vector3f original = new Vector3f(1, 0, 0);
        Vector3f translation = new Vector3f(10, 5, 0);
        Vector3f rotation = new Vector3f(0, 0, (float) Math.toRadians(90));
        Vector3f scale = new Vector3f(2, 1, 1);

        Matrix4x4 modelMatrix = GraphicConveyor.rotateScaleTranslate(translation, rotation, scale);

        Vector3f result = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, original);

        Assertions.assertEquals(10.0f, result.getX(), EPSILON);
        Assertions.assertEquals(7.0f,  result.getY(), EPSILON);
        Assertions.assertEquals(0.0f,  result.getZ(), EPSILON);
    }
}