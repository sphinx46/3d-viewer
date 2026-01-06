package ru.vsu.cs.cg.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vector2fTest {

    private static final float TEST_EPSILON = 1e-5f;

    @Test
    void testAdd() {
        Vector2f v1 = new Vector2f(1, 2);
        Vector2f v2 = new Vector2f(3, 4);
        Vector2f result = v1.add(v2);

        assertEquals(4, result.getX(), TEST_EPSILON);
        assertEquals(6, result.getY(), TEST_EPSILON);
    }

    @Test
    void testSubtract() {
        Vector2f v1 = new Vector2f(5, 7);
        Vector2f v2 = new Vector2f(2, 3);
        Vector2f result = v1.subtract(v2);

        assertEquals(3, result.getX(), TEST_EPSILON);
        assertEquals(4, result.getY(), TEST_EPSILON);
    }

    @Test
    void testMultiply() {
        Vector2f v = new Vector2f(2, 3);
        Vector2f result = v.multiply(2);

        assertEquals(4, result.getX(), TEST_EPSILON);
        assertEquals(6, result.getY(), TEST_EPSILON);
    }

    @Test
    void testDivide() {
        Vector2f v = new Vector2f(6, 8);
        Vector2f result = v.divide(2);

        assertEquals(3, result.getX(), TEST_EPSILON);
        assertEquals(4, result.getY(), TEST_EPSILON);
    }

    @Test
    void testDivideByZero() {
        Vector2f v = new Vector2f(1, 1);

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
        Vector2f v = new Vector2f(3, 4);
        assertEquals(5, v.length(), TEST_EPSILON);
    }

    @Test
    void testNormalize() {
        Vector2f v = new Vector2f(3, 4); // Длина 5
        Vector2f normalized = v.normalize();

        assertEquals(1, normalized.length(), TEST_EPSILON);
        assertEquals(0.6f, normalized.getX(), TEST_EPSILON);
        assertEquals(0.8f, normalized.getY(), TEST_EPSILON);
    }

    @Test
    void testNormalizeZeroVector() {
        Vector2f v = new Vector2f(0, 0);

        assertThrows(IllegalArgumentException.class, () -> {
            v.normalize();
        });

        Vector2f tiny = new Vector2f(1e-8f, 1e-8f);
        assertThrows(IllegalArgumentException.class, () -> {
            tiny.normalize();
        });
    }

    @Test
    void testNormalizeSafe() {
        Vector2f v1 = new Vector2f(3, 4);
        Vector2f normalized1 = v1.normalizeSafe();
        assertEquals(1, normalized1.length(), TEST_EPSILON);
        assertEquals(0.6f, normalized1.getX(), TEST_EPSILON);
        assertEquals(0.8f, normalized1.getY(), TEST_EPSILON);

        Vector2f v2 = new Vector2f(0, 0);
        Vector2f normalized2 = v2.normalizeSafe();
        assertEquals(0, normalized2.length(), TEST_EPSILON);
        assertEquals(0, normalized2.getX(), TEST_EPSILON);
        assertEquals(0, normalized2.getY(), TEST_EPSILON);
    }

    @Test
    void testDotProduct() {
        Vector2f v1 = new Vector2f(1, 2);
        Vector2f v2 = new Vector2f(3, 4);
        float dot = v1.dot(v2);

        assertEquals(11, dot, TEST_EPSILON);
    }

    @Test
    void testSetters() {
        Vector2f v = new Vector2f(1, 2);

        v.setX(10);
        v.setY(20);

        assertEquals(10, v.getX(), TEST_EPSILON);
        assertEquals(20, v.getY(), TEST_EPSILON);

        assertEquals((float)Math.sqrt(500), v.length(), TEST_EPSILON);
    }
}