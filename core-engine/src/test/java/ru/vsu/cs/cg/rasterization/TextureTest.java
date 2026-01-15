package ru.vsu.cs.cg.rasterization;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TextureTest {

    @Mock
    private Image mockImage;

    @Mock
    private PixelReader mockPixelReader;

    private Texture texture;
    private final double TEST_WIDTH = 100.0;
    private final double TEST_HEIGHT = 50.0;

    @BeforeEach
    void setUp() {
        when(mockImage.getPixelReader()).thenReturn(mockPixelReader);
        when(mockImage.getWidth()).thenReturn(TEST_WIDTH);
        when(mockImage.getHeight()).thenReturn(TEST_HEIGHT);

        texture = new Texture(mockImage);
    }

    @Test
    @DisplayName("Конструктор корректно инициализирует текстуру")
    void testConstructorInitializesCorrectly() {
        assertNotNull(texture);
        verify(mockImage).getPixelReader();
        verify(mockImage).getWidth();
        verify(mockImage).getHeight();
    }

    @Test
    @DisplayName("Конструктор выбрасывает исключение при отсутствии PixelReader")
    void testConstructorThrowsExceptionWhenNoPixelReader() {
        when(mockImage.getPixelReader()).thenReturn(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new Texture(mockImage)
        );

        assertEquals("Не удалось получить PixelReader текстуры", exception.getMessage());
    }

    @Test
    @DisplayName("Получение пикселя по нормализованным координатам")
    void testGetPixelWithNormalizedCoordinates() {
        when(mockPixelReader.getColor(anyInt(), anyInt())).thenReturn(Color.RED);

        Color result = texture.getPixel(0.5f, 0.5f);

        verify(mockPixelReader).getColor(49, 24);
        assertEquals(Color.RED, result);
    }

    @Test
    @DisplayName("Координата u=0 соответствует левому краю текстуры")
    void testLeftEdgeCoordinateUZero() {
        when(mockPixelReader.getColor(anyInt(), anyInt())).thenReturn(Color.BLUE);

        texture.getPixel(0.0f, 0.5f);

        verify(mockPixelReader).getColor(0, 24);
    }

    @Test
    @DisplayName("Координата u=1 соответствует правому краю текстуры")
    void testRightEdgeCoordinateUOne() {
        when(mockPixelReader.getColor(anyInt(), anyInt())).thenReturn(Color.GREEN);

        texture.getPixel(1.0f, 0.5f);

        verify(mockPixelReader).getColor(99, 24);
    }

    @Test
    @DisplayName("Координата v=0 соответствует нижнему краю текстуры")
    void testBottomEdgeCoordinateVZero() {
        when(mockPixelReader.getColor(anyInt(), anyInt())).thenReturn(Color.YELLOW);

        texture.getPixel(0.5f, 0.0f);

        verify(mockPixelReader).getColor(49, 49);
    }

    @Test
    @DisplayName("Координата v=1 соответствует верхнему краю текстуры")
    void testTopEdgeCoordinateVOne() {
        when(mockPixelReader.getColor(anyInt(), anyInt())).thenReturn(Color.PURPLE);

        texture.getPixel(0.5f, 1.0f);

        verify(mockPixelReader).getColor(49, 0);
    }
}