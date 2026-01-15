package ru.vsu.cs.cg.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import ru.vsu.cs.cg.model.Polygon;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PolygonTest {

    private Polygon polygon;
    private List<Integer> vertexIndices;
    private List<Integer> textureIndices;
    private List<Integer> normalIndices;

    @BeforeEach
    void setUp() {
        vertexIndices = Arrays.asList(0, 1, 2, 3, 4);
        textureIndices = Arrays.asList(10, 11, 12, 13, 14);
        normalIndices = Arrays.asList(20, 21, 22, 23, 24);

        polygon = new Polygon(vertexIndices, textureIndices, normalIndices);
    }

    @Test
    @DisplayName("Конструктор по умолчанию создает пустой полигон")
    void testDefaultConstructorCreatesEmptyPolygon() {
        Polygon emptyPolygon = new Polygon();

        assertNotNull(emptyPolygon.getVertexIndices());
        assertNotNull(emptyPolygon.getTextureVertexIndices());
        assertNotNull(emptyPolygon.getNormalIndices());

        assertTrue(emptyPolygon.getVertexIndices().isEmpty());
        assertTrue(emptyPolygon.getTextureVertexIndices().isEmpty());
        assertTrue(emptyPolygon.getNormalIndices().isEmpty());
    }

    @Test
    @DisplayName("Параметризованный конструктор правильно инициализирует полигон")
    void testParameterizedConstructorInitializesCorrectly() {
        assertEquals(vertexIndices, polygon.getVertexIndices());
        assertEquals(textureIndices, polygon.getTextureVertexIndices());
        assertEquals(normalIndices, polygon.getNormalIndices());
    }

    @Test
    @DisplayName("Триангуляция пятиугольника возвращает три треугольника")
    void testTriangulatePentagonReturnsThreeTriangles() {
        List<Polygon> triangles = polygon.triangulate();

        assertEquals(3, triangles.size());

        // Проверяем первый треугольник (0-1-2)
        Polygon firstTriangle = triangles.get(0);
        assertEquals(Arrays.asList(0, 1, 2), firstTriangle.getVertexIndices());
        assertEquals(Arrays.asList(10, 11, 12), firstTriangle.getTextureVertexIndices());
        assertEquals(Arrays.asList(20, 21, 22), firstTriangle.getNormalIndices());

        // Проверяем второй треугольник (0-2-3)
        Polygon secondTriangle = triangles.get(1);
        assertEquals(Arrays.asList(0, 2, 3), secondTriangle.getVertexIndices());
        assertEquals(Arrays.asList(10, 12, 13), secondTriangle.getTextureVertexIndices());
        assertEquals(Arrays.asList(20, 22, 23), secondTriangle.getNormalIndices());

        // Проверяем третий треугольник (0-3-4)
        Polygon thirdTriangle = triangles.get(2);
        assertEquals(Arrays.asList(0, 3, 4), thirdTriangle.getVertexIndices());
        assertEquals(Arrays.asList(10, 13, 14), thirdTriangle.getTextureVertexIndices());
        assertEquals(Arrays.asList(20, 23, 24), thirdTriangle.getNormalIndices());
    }

    @Test
    @DisplayName("Триангуляция треугольника возвращает один треугольник")
    void testTriangulateTriangleReturnsOneTriangle() {
        Polygon triangle = new Polygon(
                Arrays.asList(0, 1, 2),
                Arrays.asList(10, 11, 12),
                Arrays.asList(20, 21, 22)
        );

        List<Polygon> result = triangle.triangulate();

        assertEquals(1, result.size());
        assertEquals(Arrays.asList(0, 1, 2), result.get(0).getVertexIndices());
        assertEquals(Arrays.asList(10, 11, 12), result.get(0).getTextureVertexIndices());
        assertEquals(Arrays.asList(20, 21, 22), result.get(0).getNormalIndices());
    }

    @Test
    @DisplayName("Триангуляция четырехугольника возвращает два треугольника")
    void testTriangulateQuadReturnsTwoTriangles() {
        Polygon quad = new Polygon(
                Arrays.asList(0, 1, 2, 3),
                Arrays.asList(10, 11, 12, 13),
                Arrays.asList(20, 21, 22, 23)
        );

        List<Polygon> result = quad.triangulate();

        assertEquals(2, result.size());
        assertEquals(Arrays.asList(0, 1, 2), result.get(0).getVertexIndices());
        assertEquals(Arrays.asList(0, 2, 3), result.get(1).getVertexIndices());
    }

    @Test
    @DisplayName("Триангуляция полигона без текстурных координат")
    void testTriangulateWithoutTextureCoordinates() {
        Polygon polygonWithoutTextures = new Polygon(
                Arrays.asList(0, 1, 2, 3),
                Collections.emptyList(),
                Arrays.asList(20, 21, 22, 23)
        );

        List<Polygon> result = polygonWithoutTextures.triangulate();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getTextureVertexIndices().isEmpty());
        assertTrue(result.get(1).getTextureVertexIndices().isEmpty());
    }

    @Test
    @DisplayName("Триангуляция полигона без нормалей")
    void testTriangulateWithoutNormals() {
        Polygon polygonWithoutNormals = new Polygon(
                Arrays.asList(0, 1, 2, 3),
                Arrays.asList(10, 11, 12, 13),
                Collections.emptyList()
        );

        List<Polygon> result = polygonWithoutNormals.triangulate();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getNormalIndices().isEmpty());
        assertTrue(result.get(1).getNormalIndices().isEmpty());
    }
}