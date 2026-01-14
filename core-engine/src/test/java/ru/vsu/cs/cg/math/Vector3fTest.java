package ru.vsu.cs.cg.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vector3fTest {

    @Test
    void testAdd() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        Vector3f result = v1.add(v2);

        assertEquals(new Vector3f(5, 7, 9), result);
    }

    @Test
    void testSubtract() {
        Vector3f v1 = new Vector3f(10, 20, 30);
        Vector3f v2 = new Vector3f(1, 2, 3);
        Vector3f result = v1.subtract(v2);

        assertEquals(new Vector3f(9, 18, 27), result);
    }

    @Test
    void testMultiplyScalar() {
        Vector3f v = new Vector3f(2, 3, 4);
        Vector3f result = v.multiply(2);

        assertEquals(new Vector3f(4, 6, 8), result);
    }

    @Test
    void testMultiplyVector() {
        Vector3f v1 = new Vector3f(2, 3, 4);
        Vector3f v2 = new Vector3f(3, 2, 1);
        Vector3f result = v1.multiply(v2);

        assertEquals(new Vector3f(6, 6, 4), result);
    }

    @Test
    void testDivide() {
        Vector3f v = new Vector3f(6, 9, 12);
        Vector3f result = v.divide(3);

        assertEquals(new Vector3f(2, 3, 4), result);
    }

    @Test
    void testDivideByZero() {
        Vector3f v = new Vector3f(1, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> v.divide(0));

        assertThrows(IllegalArgumentException.class, () -> v.divide(1e-8f));
        assertDoesNotThrow(() -> v.divide(1e-6f));
    }

    @Test
    void testLength() {
        Vector3f v = new Vector3f(2, 3, 6);
        assertEquals(7, v.length(), 1e-5f);
    }

    @Test
    void testLengthZero() {
        Vector3f v = new Vector3f(0, 0, 0);
        assertEquals(0, v.length(), 1e-5f);
    }

    @Test
    void testNormalized() {
        Vector3f v = new Vector3f(2, 0, 0);
        Vector3f result = v.normalized();

        assertEquals(new Vector3f(1, 0, 0), result);
    }

    @Test
    void testNormalizedComplex() {
        Vector3f v = new Vector3f(1, 2, 2);
        Vector3f result = v.normalized();

        assertEquals(new Vector3f(1.0f/3.0f, 2.0f/3.0f, 2.0f/3.0f), result);
    }

    @Test
    void testNormalizedZeroVector() {
        Vector3f v = new Vector3f(0, 0, 0);
        assertThrows(IllegalArgumentException.class, v::normalized);

        Vector3f tiny = new Vector3f(1e-8f, 1e-8f, 1e-8f);
        assertThrows(IllegalArgumentException.class, tiny::normalized);
    }

//    @Test
//    void testNormalizeSafe() {
//        Vector3f v1 = new Vector3f(2, 0, 0);
//        assertEquals(new Vector3f(1, 0, 0), v1.normalizeSafe());
//
//        Vector3f v2 = new Vector3f(0, 0, 0);
//        assertEquals(new Vector3f(0, 0, 0), v2.normalizeSafe());
//    }

    @Test
    void testDotProduct() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);

        assertEquals(32, v1.dot(v2), 1e-5f);
    }

    @Test
    void testCrossProduct() {
        Vector3f i = new Vector3f(1, 0, 0);
        Vector3f j = new Vector3f(0, 1, 0);
        Vector3f k = new Vector3f(0, 0, 1);

        assertEquals(k, i.cross(j));
        assertEquals(i, j.cross(k));
        assertEquals(j, k.cross(i));
    }

    @Test
    void testCrossProductCalculated() {
        Vector3f v1 = new Vector3f(2, 3, 4);
        Vector3f v2 = new Vector3f(5, 6, 7);

        assertEquals(new Vector3f(-3, 6, -3), v1.cross(v2));
    }

    @Test
    void testCrossProductOrthogonal() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        Vector3f cross = v1.cross(v2);

        assertEquals(0, cross.dot(v1), 1e-5f);
        assertEquals(0, cross.dot(v2), 1e-5f);
    }
}