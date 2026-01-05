package ru.vsu.cs.cg.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Matrix3x3Test {

    private static final float TEST_EPSILON = 1e-5f;

    @Test
    void testDefaultConstructorCreatesIdentityMatrix() {
        Matrix3x3 m = new Matrix3x3();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
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
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Matrix3x3 m = new Matrix3x3(values);

        assertEquals(1, m.get(0, 0), TEST_EPSILON);
        assertEquals(2, m.get(0, 1), TEST_EPSILON);
        assertEquals(3, m.get(0, 2), TEST_EPSILON);
        assertEquals(4, m.get(1, 0), TEST_EPSILON);
        assertEquals(5, m.get(1, 1), TEST_EPSILON);
        assertEquals(6, m.get(1, 2), TEST_EPSILON);
        assertEquals(7, m.get(2, 0), TEST_EPSILON);
        assertEquals(8, m.get(2, 1), TEST_EPSILON);
        assertEquals(9, m.get(2, 2), TEST_EPSILON);
    }

    @Test
    void testConstructorInvalidArray() {
        float[][] invalid1 = {{1, 2}, {3, 4}};
        float[][] invalid2 = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}};
        assertThrows(IllegalArgumentException.class, () -> new Matrix3x3(null));
        assertThrows(IllegalArgumentException.class, () -> new Matrix3x3(invalid1));
        assertThrows(IllegalArgumentException.class, () -> new Matrix3x3(invalid2));
    }

    @Test
    void testMakeIdentity() {
        Matrix3x3 m = new Matrix3x3();
        m.set(0, 0, 5);
        m.makeIdentity();

        assertEquals(1, m.get(0, 0), TEST_EPSILON);
        assertEquals(0, m.get(0, 1), TEST_EPSILON);
        assertEquals(1, m.get(1, 1), TEST_EPSILON);
        assertEquals(0, m.get(2, 0), TEST_EPSILON);
    }

    @Test
    void testMakeZero() {
        Matrix3x3 m = new Matrix3x3();
        m.makeZero();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(0.0f, m.get(i, j), TEST_EPSILON);
            }
        }
    }

    @Test
    void testAdd() {
        float[][] values1 = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        float[][] values2 = {
                {9, 8, 7},
                {6, 5, 4},
                {3, 2, 1}
        };

        Matrix3x3 m1 = new Matrix3x3(values1);
        Matrix3x3 m2 = new Matrix3x3(values2);
        Matrix3x3 result = m1.add(m2);


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(10.0f, result.get(i, j), TEST_EPSILON);
            }
        }
    }

    @Test
    void testSubtract() {
        float[][] values1 = {
                {10, 10, 10},
                {10, 10, 10},
                {10, 10, 10}
        };
        float[][] values2 = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        Matrix3x3 m1 = new Matrix3x3(values1);
        Matrix3x3 m2 = new Matrix3x3(values2);
        Matrix3x3 result = m1.subtract(m2);

        assertEquals(9, result.get(0, 0), TEST_EPSILON);
        assertEquals(8, result.get(0, 1), TEST_EPSILON);
        assertEquals(6, result.get(1, 0), TEST_EPSILON);
        assertEquals(1, result.get(2, 2), TEST_EPSILON);
    }

    @Test
    void testMultiplyByVector() {
        float[][] values = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Matrix3x3 m = new Matrix3x3(values);
        Vector3f v = new Vector3f(2, 3, 4);

        Vector3f result = m.multiply(v);

        assertEquals(20, result.getX(), TEST_EPSILON);
        assertEquals(47, result.getY(), TEST_EPSILON);
        assertEquals(74, result.getZ(), TEST_EPSILON);
    }

    @Test
    void testMultiplyByIdentityVector() {
        Matrix3x3 identity = new Matrix3x3();
        Vector3f v = new Vector3f(2, 3, 4);

        Vector3f result = identity.multiply(v);

        assertEquals(2, result.getX(), TEST_EPSILON);
        assertEquals(3, result.getY(), TEST_EPSILON);
        assertEquals(4, result.getZ(), TEST_EPSILON);
    }

    @Test
    void testMultiplyMatrices() {
        float[][] values1 = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        float[][] values2 = {
                {9, 8, 7},
                {6, 5, 4},
                {3, 2, 1}
        };

        Matrix3x3 m1 = new Matrix3x3(values1);
        Matrix3x3 m2 = new Matrix3x3(values2);
        Matrix3x3 result = m1.multiply(m2);

        assertEquals(30, result.get(0, 0), TEST_EPSILON);
        assertEquals(24, result.get(0, 1), TEST_EPSILON);
        assertEquals(18, result.get(0, 2), TEST_EPSILON);

        assertEquals(84, result.get(1, 0), TEST_EPSILON);
        assertEquals(90, result.get(2, 2), TEST_EPSILON);
    }

    @Test
    void testMultiplyByIdentityMatrix() {
        float[][] values = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Matrix3x3 m = new Matrix3x3(values);
        Matrix3x3 identity = new Matrix3x3();

        Matrix3x3 result1 = m.multiply(identity);
        Matrix3x3 result2 = identity.multiply(m);

        assertEquals(m, result1);
        assertEquals(m, result2);
    }

    @Test
    void testTranspose() {
        float[][] values = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Matrix3x3 m = new Matrix3x3(values);
        Matrix3x3 transposed = m.transpose();

        assertEquals(1, transposed.get(0, 0), TEST_EPSILON);
        assertEquals(4, transposed.get(0, 1), TEST_EPSILON);
        assertEquals(2, transposed.get(1, 0), TEST_EPSILON);
        assertEquals(5, transposed.get(1, 1), TEST_EPSILON);
        assertEquals(9, transposed.get(2, 2), TEST_EPSILON);
    }

    @Test
    void testTransposeOfTranspose() {
        float[][] values = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Matrix3x3 m = new Matrix3x3(values);
        Matrix3x3 transposedTwice = m.transpose().transpose();

        assertEquals(m, transposedTwice);
    }

    @Test
    void testTransposeOfIdentity() {
        Matrix3x3 identity = new Matrix3x3();
        Matrix3x3 transposed = identity.transpose();

        assertEquals(identity, transposed);
    }

    @Test
    void testSetAndGet() {
        Matrix3x3 m = new Matrix3x3();

        m.set(0, 0, 10);
        m.set(1, 2, 20);
        m.set(2, 1, 30);

        assertEquals(10, m.get(0, 0), TEST_EPSILON);
        assertEquals(20, m.get(1, 2), TEST_EPSILON);
        assertEquals(30, m.get(2, 1), TEST_EPSILON);
    }

    @Test
    void testSetInvalidIndex() {
        Matrix3x3 m = new Matrix3x3();

        assertThrows(IllegalArgumentException.class, () -> m.set(-1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(3, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(0, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(0, 3, 1));
    }

    @Test
    void testGetInvalidIndex() {
        Matrix3x3 m = new Matrix3x3();

        assertThrows(IllegalArgumentException.class, () -> m.get(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> m.get(3, 0));
    }

    @Test
    void testEquals() {
        float[][] values1 = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        float[][] values2 = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        float[][] values3 = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 10}
        };

        Matrix3x3 m1 = new Matrix3x3(values1);
        Matrix3x3 m2 = new Matrix3x3(values2);
        Matrix3x3 m3 = new Matrix3x3(values3);

        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
    }
}
