package ru.vsu.cs.cg.rasterization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ZBufferTest {

    private ZBuffer zBuffer;
    private final int WIDTH = 10;
    private final int HEIGHT = 10;

    @BeforeEach
    void setUp() {
        zBuffer = new ZBuffer(WIDTH, HEIGHT);
    }

    @Test
    void testConstructorValidDimensions() {
        assertNotNull(zBuffer);
        assertEquals(WIDTH, zBuffer.getWidth());
        assertEquals(HEIGHT, zBuffer.getHeight());
    }

    @Test
    void testConstructorZeroDimensions() {
        ZBuffer zeroBuffer = new ZBuffer(0, 0);
        assertEquals(0, zeroBuffer.getWidth());
        assertEquals(0, zeroBuffer.getHeight());
    }

    @Test
    void testConstructorNegativeDimensionsThrowsException() {
        assertThrows(NegativeArraySizeException.class, () -> {
            new ZBuffer(-1, 10);
        });

        assertThrows(NegativeArraySizeException.class, () -> {
            new ZBuffer(10, -1);
        });
    }

    @Test
    void testClearBufferFilledWithMaxValue() {
        assertTrue(zBuffer.checkAndSet(0, 0, 0.5f));
        assertTrue(zBuffer.checkAndSet(5, 5, 0.3f));

        zBuffer.clear();

        assertFalse(zBuffer.checkAndSet(0, 0, Float.MAX_VALUE - 1));

        assertTrue(zBuffer.checkAndSet(0, 0, 0.1f));
    }

    @Test
    void testCheckAndSetValidCoordinates() {
        assertTrue(zBuffer.checkAndSet(0, 0, 1.0f));
        assertTrue(zBuffer.checkAndSet(WIDTH - 1, 0, 0.5f));
        assertTrue(zBuffer.checkAndSet(0, HEIGHT - 1, 0.3f));
        assertTrue(zBuffer.checkAndSet(WIDTH - 1, HEIGHT - 1, 0.1f));

        assertFalse(zBuffer.checkAndSet(0, 0, 1.5f));
    }
}
