package ru.vsu.cs.cg.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Matrix4x4Test {

    private static final float TEST_EPSILON = 1e-5f;

    @Test
    void testDefaultConstructorCreatesIdentityMatrix() {
        Matrix4x4 m = new Matrix4x4();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    assertEquals(1.0f, m.get(i, j), TEST_EPSILON);
                } else {
                    assertEquals(0.0f, m.get(i, j), TEST_EPSILON);
                }
            }
        }
    }

    @Test
    void testConstructorFromArray() {
        float[][] values = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4x4 m = new Matrix4x4(values);

        assertEquals(1, m.get(0, 0), TEST_EPSILON);
        assertEquals(6, m.get(1, 1), TEST_EPSILON);
        assertEquals(11, m.get(2, 2), TEST_EPSILON);
        assertEquals(16, m.get(3, 3), TEST_EPSILON);
        assertEquals(5, m.get(1, 0), TEST_EPSILON);
        assertEquals(10, m.get(2, 1), TEST_EPSILON);
    }

    @Test
    void testConstructorInvalidArray() {
        float[][] invalid1 = {{1, 2}, {3, 4}};
        float[][] invalid2 = {
                {1, 2, 3, 4, 5},
                {6, 7, 8, 9, 10},
                {11, 12, 13, 14, 15},
                {16, 17, 18, 19, 20}
        };

        assertThrows(IllegalArgumentException.class, () -> new Matrix4x4(null));
        assertThrows(IllegalArgumentException.class, () -> new Matrix4x4(invalid1));
        assertThrows(IllegalArgumentException.class, () -> new Matrix4x4(invalid2));
    }

    @Test
    void testMakeIdentity() {
        Matrix4x4 m = new Matrix4x4();
        m.set(0, 0, 5);
        m.makeIdentity();

        assertEquals(1, m.get(0, 0), TEST_EPSILON);
        assertEquals(0, m.get(0, 1), TEST_EPSILON);
        assertEquals(1, m.get(1, 1), TEST_EPSILON);
        assertEquals(0, m.get(3, 0), TEST_EPSILON);
        assertEquals(1, m.get(3, 3), TEST_EPSILON);
    }

    @Test
    void testMakeZero() {
        Matrix4x4 m = new Matrix4x4();
        m.makeZero();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(0.0f, m.get(i, j), TEST_EPSILON);
            }
        }
    }

    @Test
    void testAdd() {
        float[][] values1 = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        float[][] values2 = {
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        };

        Matrix4x4 m1 = new Matrix4x4(values1);
        Matrix4x4 m2 = new Matrix4x4(values2);
        Matrix4x4 result = m1.add(m2);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(17.0f, result.get(i, j), TEST_EPSILON);
            }
        }
    }

    @Test
    void testSubtract() {
        float[][] values1 = {
                {10, 10, 10, 10},
                {10, 10, 10, 10},
                {10, 10, 10, 10},
                {10, 10, 10, 10}
        };
        float[][] values2 = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };

        Matrix4x4 m1 = new Matrix4x4(values1);
        Matrix4x4 m2 = new Matrix4x4(values2);
        Matrix4x4 result = m1.subtract(m2);

        assertEquals(9, result.get(0, 0), TEST_EPSILON);
        assertEquals(8, result.get(0, 1), TEST_EPSILON);
        assertEquals(5, result.get(1, 0), TEST_EPSILON);
        assertEquals(-6, result.get(3, 3), TEST_EPSILON);
    }

    @Test
    void testMultiplyByVector() {
        float[][] values = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4x4 m = new Matrix4x4(values);
        Vector4f v = new Vector4f(2, 3, 4, 5);

        Vector4f result = m.multiply(v);

        assertEquals(40, result.getX(), TEST_EPSILON);
        assertEquals(96, result.getY(), TEST_EPSILON);
        assertEquals(152, result.getZ(), TEST_EPSILON);
        assertEquals(208, result.getW(), TEST_EPSILON);
    }

    @Test
    void testMultiplyByIdentityVector() {
        Matrix4x4 identity = new Matrix4x4();
        Vector4f v = new Vector4f(2, 3, 4, 5);

        Vector4f result = identity.multiply(v);

        assertEquals(2, result.getX(), TEST_EPSILON);
        assertEquals(3, result.getY(), TEST_EPSILON);
        assertEquals(4, result.getZ(), TEST_EPSILON);
        assertEquals(5, result.getW(), TEST_EPSILON);
    }

    @Test
    void testMultiplyMatrices() {
        float[][] values1 = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        float[][] values2 = {
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        };

        Matrix4x4 m1 = new Matrix4x4(values1);
        Matrix4x4 m2 = new Matrix4x4(values2);
        Matrix4x4 result = m1.multiply(m2);

        assertEquals(80, result.get(0, 0), TEST_EPSILON);
        assertEquals(70, result.get(0, 1), TEST_EPSILON);
        assertEquals(240, result.get(1, 0), TEST_EPSILON);
        assertEquals(386, result.get(3, 3), TEST_EPSILON);
    }

    @Test
    void testMultiplyByIdentityMatrix() {
        float[][] values = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4x4 m = new Matrix4x4(values);
        Matrix4x4 identity = new Matrix4x4();

        Matrix4x4 result1 = m.multiply(identity);
        Matrix4x4 result2 = identity.multiply(m);

        assertEquals(m, result1);
        assertEquals(m, result2);
    }

    @Test
    void testTranspose() {
        float[][] values = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4x4 m = new Matrix4x4(values);
        Matrix4x4 transposed = m.transpose();

        assertEquals(1, transposed.get(0, 0), TEST_EPSILON);
        assertEquals(5, transposed.get(0, 1), TEST_EPSILON);
        assertEquals(2, transposed.get(1, 0), TEST_EPSILON);
        assertEquals(6, transposed.get(1, 1), TEST_EPSILON);
        assertEquals(11, transposed.get(2, 2), TEST_EPSILON);
        assertEquals(16, transposed.get(3, 3), TEST_EPSILON);
    }

    @Test
    void testTransposeOfTranspose() {
        float[][] values = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4x4 m = new Matrix4x4(values);
        Matrix4x4 transposedTwice = m.transpose().transpose();

        assertEquals(m, transposedTwice);
    }

    @Test
    void testTransposeOfIdentity() {
        Matrix4x4 identity = new Matrix4x4();
        Matrix4x4 transposed = identity.transpose();

        assertEquals(identity, transposed);
    }

    @Test
    void testSetAndGet() {
        Matrix4x4 m = new Matrix4x4();

        m.set(0, 0, 10);
        m.set(1, 2, 20);
        m.set(2, 3, 30);
        m.set(3, 1, 40);

        assertEquals(10, m.get(0, 0), TEST_EPSILON);
        assertEquals(20, m.get(1, 2), TEST_EPSILON);
        assertEquals(30, m.get(2, 3), TEST_EPSILON);
        assertEquals(40, m.get(3, 1), TEST_EPSILON);
    }

    @Test
    void testSetInvalidIndex() {
        Matrix4x4 m = new Matrix4x4();

        assertThrows(IllegalArgumentException.class, () -> m.set(-1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(4, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(0, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(0, 4, 1));
    }

    @Test
    void testGetInvalidIndex() {
        Matrix4x4 m = new Matrix4x4();

        assertThrows(IllegalArgumentException.class, () -> m.get(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> m.get(4, 0));
    }

    @Test
    void testEquals() {
        float[][] values1 = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        float[][] values2 = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        float[][] values3 = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 17}
        };

        Matrix4x4 m1 = new Matrix4x4(values1);
        Matrix4x4 m2 = new Matrix4x4(values2);
        Matrix4x4 m3 = new Matrix4x4(values3);

        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
    }
}