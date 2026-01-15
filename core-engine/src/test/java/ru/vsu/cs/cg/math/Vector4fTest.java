package ru.vsu.cs.cg.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vector4fTest {
    
    private static final float TEST_EPSILON = 1e-5f;

    @Test
    void testAdd() {
        Vector4f v1 = new Vector4f(1, 2, 3, 4);
        Vector4f v2 = new Vector4f(5, 6, 7, 8);
        Vector4f result = v1.add(v2);

        assertEquals(6, result.getX(), TEST_EPSILON);
        assertEquals(8, result.getY(), TEST_EPSILON);
        assertEquals(10, result.getZ(), TEST_EPSILON);
        assertEquals(12, result.getW(), TEST_EPSILON);
    }

    @Test
    void testSubtract() {
        Vector4f v1 = new Vector4f(10, 20, 30, 40);
        Vector4f v2 = new Vector4f(1, 2, 3, 4);
        Vector4f result = v1.subtract(v2);

        assertEquals(9, result.getX(), TEST_EPSILON);
        assertEquals(18, result.getY(), TEST_EPSILON);
        assertEquals(27, result.getZ(), TEST_EPSILON);
        assertEquals(36, result.getW(), TEST_EPSILON);
    }

    @Test
    void testMultiply() {
        Vector4f v = new Vector4f(2, 3, 4, 5);
        Vector4f result = v.multiply(2);

        assertEquals(4, result.getX(), TEST_EPSILON);
        assertEquals(6, result.getY(), TEST_EPSILON);
        assertEquals(8, result.getZ(), TEST_EPSILON);
        assertEquals(10, result.getW(), TEST_EPSILON);
    }

    @Test
    void testDivide() {
        Vector4f v = new Vector4f(8, 12, 16, 20);
        Vector4f result = v.divide(4);

        assertEquals(2, result.getX(), TEST_EPSILON);
        assertEquals(3, result.getY(), TEST_EPSILON);
        assertEquals(4, result.getZ(), TEST_EPSILON);
        assertEquals(5, result.getW(), TEST_EPSILON);
    }

    @Test
    void testDivideByZero() {
        Vector4f v = new Vector4f(1, 1, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> {
            v.divide(0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            v.divide(1e-8f);
        });

        assertDoesNotThrow(() -> {
            v.divide(1e-6f);
        });
    }

    @Test
    void testLength() {
        Vector4f v = new Vector4f(1, 2, 2, 4);
        // 1 + 4 + 4 + 16 = 25, sqrt(25) = 5
        assertEquals(5, v.length(), TEST_EPSILON);
    }

    @Test
    void testLengthZero() {
        Vector4f v = new Vector4f(0, 0, 0, 0);
        assertEquals(0, v.length(), TEST_EPSILON);
    }

    @Test
    void testNormalize() {
        Vector4f v = new Vector4f(2, 0, 0, 0);
        Vector4f normalized = v.normalize();

        assertEquals(1, normalized.length(), TEST_EPSILON);
        assertEquals(1, normalized.getX(), TEST_EPSILON);
        assertEquals(0, normalized.getY(), TEST_EPSILON);
        assertEquals(0, normalized.getZ(), TEST_EPSILON);
        assertEquals(0, normalized.getW(), TEST_EPSILON);
    }

    @Test
    void testNormalizeComplex() {
        Vector4f v = new Vector4f(1, 1, 1, 1);
        Vector4f normalized = v.normalize();

        assertEquals(1, normalized.length(), TEST_EPSILON);
        assertEquals(0.5f, normalized.getX(), TEST_EPSILON);
        assertEquals(0.5f, normalized.getY(), TEST_EPSILON);
        assertEquals(0.5f, normalized.getZ(), TEST_EPSILON);
        assertEquals(0.5f, normalized.getW(), TEST_EPSILON);
    }

    @Test
    void testNormalizeZeroVector() {
        Vector4f v = new Vector4f(0, 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> {
            v.normalize();
        });

        Vector4f tiny = new Vector4f(1e-8f, 1e-8f, 1e-8f, 1e-8f);
        assertThrows(IllegalArgumentException.class, () -> {
            tiny.normalize();
        });
    }

    @Test
    void testNormalizeSafe() {
        Vector4f v1 = new Vector4f(2, 0, 0, 0);
        Vector4f normalized1 = v1.normalizeSafe();
        assertEquals(1, normalized1.length(), TEST_EPSILON);
        assertEquals(1, normalized1.getX(), TEST_EPSILON);
        assertEquals(0, normalized1.getY(), TEST_EPSILON);

        Vector4f v2 = new Vector4f(0, 0, 0, 0);
        Vector4f normalized2 = v2.normalizeSafe();
        assertEquals(0, normalized2.length(), TEST_EPSILON);
        assertEquals(0, normalized2.getX(), TEST_EPSILON);

        Vector4f v3 = new Vector4f(1e-8f, 1e-8f, 1e-8f, 1e-8f);
        Vector4f normalized3 = v3.normalizeSafe();
        assertEquals(1e-8f, normalized3.getX(), 1e-10f); // Используем малый допуск тут
        assertEquals(1e-8f, normalized3.getY(), 1e-10f);
        assertEquals(1e-8f, normalized3.getZ(), 1e-10f);
        assertEquals(1e-8f, normalized3.getW(), 1e-10f);
    }

    @Test
    void testDotProduct() {
        Vector4f v1 = new Vector4f(1, 2, 3, 4);
        Vector4f v2 = new Vector4f(5, 6, 7, 8);
        float dot = v1.dot(v2);

        assertEquals(70, dot, TEST_EPSILON);
    }

    @Test
    void testDotProductWithZero() {
        Vector4f v1 = new Vector4f(1, 2, 3, 4);
        Vector4f v2 = new Vector4f(0, 0, 0, 0);
        float dot = v1.dot(v2);

        assertEquals(0, dot, TEST_EPSILON);
    }

    @Test
    void testDotProductOrthogonal() {
        Vector4f v1 = new Vector4f(1, 0, 0, 0);
        Vector4f v2 = new Vector4f(0, 1, 0, 0);

        float dot = v1.dot(v2);
        assertEquals(0, dot, TEST_EPSILON);
    }

    @Test
    void testDotProductSameVector() {
        Vector4f v = new Vector4f(1, 2, 3, 4);
        float dot = v.dot(v);
        float lengthSquared = v.length() * v.length();

        assertEquals(lengthSquared, dot, TEST_EPSILON);
    }

    @Test
    void testSetters() {
        Vector4f v = new Vector4f(1, 2, 3, 4);

        v.setX(10);
        v.setY(20);
        v.setZ(30);
        v.setW(40);

        assertEquals(10, v.getX(), TEST_EPSILON);
        assertEquals(20, v.getY(), TEST_EPSILON);
        assertEquals(30, v.getZ(), TEST_EPSILON);
        assertEquals(40, v.getW(), TEST_EPSILON);

        assertEquals((float)Math.sqrt(3000), v.length(), TEST_EPSILON);
    }

    @Test
    void testToVector3() {
        Vector4f v1 = new Vector4f(2, 4, 6, 1);
        Vector3f result1 = v1.toVector3();
        assertEquals(2, result1.getX(), TEST_EPSILON);
        assertEquals(4, result1.getY(), TEST_EPSILON);
        assertEquals(6, result1.getZ(), TEST_EPSILON);

        Vector4f v2 = new Vector4f(2, 4, 6, 2);
        Vector3f result2 = v2.toVector3();
        assertEquals(1, result2.getX(), TEST_EPSILON);
        assertEquals(2, result2.getY(), TEST_EPSILON);
        assertEquals(3, result2.getZ(), TEST_EPSILON);

        Vector4f v3 = new Vector4f(2, 4, 6, 0.5f);
        Vector3f result3 = v3.toVector3();
        assertEquals(4, result3.getX(), TEST_EPSILON);
        assertEquals(8, result3.getY(), TEST_EPSILON);
        assertEquals(12, result3.getZ(), TEST_EPSILON);

        Vector4f v4 = new Vector4f(2, 4, 6, -2);
        Vector3f result4 = v4.toVector3();
        assertEquals(-1, result4.getX(), TEST_EPSILON);
        assertEquals(-2, result4.getY(), TEST_EPSILON);
        assertEquals(-3, result4.getZ(), TEST_EPSILON);
    }

    @Test
    void testToVector3WithZeroW() {
        Vector4f v = new Vector4f(2, 4, 6, 0);

        assertThrows(IllegalArgumentException.class, () -> {
            v.toVector3();
        });

        Vector4f tiny = new Vector4f(2, 4, 6, 1e-8f);
        assertThrows(IllegalArgumentException.class, () -> {
            tiny.toVector3();
        });

        Vector4f small = new Vector4f(2, 4, 6, 1e-6f);
        assertDoesNotThrow(() -> {
            small.toVector3();
        });
    }

    @Test
    void testToVector3Safe() {
        Vector4f v1 = new Vector4f(2, 4, 6, 2);
        Vector3f result1 = v1.toVector3Safe();
        assertEquals(1, result1.getX(), TEST_EPSILON);
        assertEquals(2, result1.getY(), TEST_EPSILON);
        assertEquals(3, result1.getZ(), TEST_EPSILON);

        Vector4f v2 = new Vector4f(2, 4, 6, 0);
        Vector3f result2 = v2.toVector3Safe();
        assertEquals(2, result2.getX(), TEST_EPSILON);
        assertEquals(4, result2.getY(), TEST_EPSILON);
        assertEquals(6, result2.getZ(), TEST_EPSILON);

        Vector4f v3 = new Vector4f(2, 4, 6, 1e-8f);
        Vector3f result3 = v3.toVector3Safe();
        assertEquals(2, result3.getX(), TEST_EPSILON);
        assertEquals(4, result3.getY(), TEST_EPSILON);
        assertEquals(6, result3.getZ(), TEST_EPSILON);
    }

    @Test
    void testToVector3PreservesDirection() {
        Vector4f v1 = new Vector4f(1, 2, 3, 2);
        Vector4f v2 = new Vector4f(2, 4, 6, 4);

        Vector3f result1 = v1.toVector3();
        Vector3f result2 = v2.toVector3();

        assertEquals(result1.getX(), result2.getX(), TEST_EPSILON);
        assertEquals(result1.getY(), result2.getY(), TEST_EPSILON);
        assertEquals(result1.getZ(), result2.getZ(), TEST_EPSILON);
    }

    @Test
    void testChainedOperationsWithToVector3() {
        Vector4f v = new Vector4f(10, 20, 30, 2);

        Vector3f result = v.toVector3().multiply(2);

        assertEquals(10, result.getX(), TEST_EPSILON);
        assertEquals(20, result.getY(), TEST_EPSILON);
        assertEquals(30, result.getZ(), TEST_EPSILON);
    }
}