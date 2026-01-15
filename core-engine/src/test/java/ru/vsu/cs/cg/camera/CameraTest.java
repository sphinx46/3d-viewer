package ru.vsu.cs.cg.camera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    private Camera camera;
    private final String TEST_ID = "TestCamera";
    private final Vector3f TEST_POSITION = new Vector3f(0, 0, 5);
    private final Vector3f TEST_TARGET = new Vector3f(0, 0, 0);

    @BeforeEach
    void setUp() {
        camera = new Camera(TEST_ID, TEST_POSITION, TEST_TARGET);
    }

    @Test
    @DisplayName("Конструктор по умолчанию создает камеру с начальными значениями")
    void testDefaultConstructorCreatesCameraWithDefaults() {
        Camera defaultCamera = new Camera();

        assertEquals("DefaultCamera_0", defaultCamera.getId());
        assertEquals(new Vector3f(0, 2, 5), defaultCamera.getPosition());
        assertEquals(new Vector3f(0, 0, 0), defaultCamera.getTarget());
        assertEquals(new Vector3f(0, 1, 0), defaultCamera.getUp());
    }

    @Test
    @DisplayName("Получение матрицы вида возвращает не-null матрицу")
    void testGetViewMatrixReturnsNotNullMatrix() {
        Matrix4x4 viewMatrix = camera.getViewMatrix();

        assertNotNull(viewMatrix);

        boolean allZero = true;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(viewMatrix.get(i, j)) > 0.0001f) {
                    allZero = false;
                    break;
                }
            }
        }
        assertFalse(allZero, "Матрица вида не должна быть нулевой");
    }

    @Test
    @DisplayName("Получение матрицы проекции возвращает не-null матрицу")
    void testGetProjectionMatrixReturnsNotNullMatrix() {
        Matrix4x4 projectionMatrix = camera.getProjectionMatrix();

        assertNotNull(projectionMatrix);

        assertNotEquals(0.0f, projectionMatrix.get(3, 2), 0.0001f);
    }

    @Test
    @DisplayName("Получение направления света возвращает нормализованный вектор")
    void testGetLightDirectionReturnsNormalizedVector() {
        Vector3f lightDirection = camera.getLightDirection();

        assertNotNull(lightDirection);

        float length = (float) Math.sqrt(
                lightDirection.getX() * lightDirection.getX() +
                        lightDirection.getY() * lightDirection.getY() +
                        lightDirection.getZ() * lightDirection.getZ()
        );

        assertEquals(1.0f, length, 0.001f, "Вектор направления света должен быть нормализован");

        assertEquals(0.0f, lightDirection.getX(), 0.001f);
        assertEquals(0.0f, lightDirection.getY(), 0.001f);
        assertEquals(1.0f, lightDirection.getZ(), 0.001f);
    }

}