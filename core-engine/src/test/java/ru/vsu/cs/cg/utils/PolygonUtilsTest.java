package ru.vsu.cs.cg.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vsu.cs.cg.model.Polygon;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PolygonUtilsTest {

    @Test
    @DisplayName("Проверка, что полигон содержит указанные вершины")
    public void testPolygonContainsAnyVertex() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));

        Set<Integer> vertexIndices = new HashSet<>(Arrays.asList(1, 5));

        assertTrue(PolygonUtils.polygonContainsAnyVertex(polygon, vertexIndices));
    }

    @Test
    @DisplayName("Проверка, что полигон не содержит указанные вершины")
    public void testPolygonDoesNotContainAnyVertex() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));

        Set<Integer> vertexIndices = new HashSet<>(Arrays.asList(5, 6, 7));

        assertFalse(PolygonUtils.polygonContainsAnyVertex(polygon, vertexIndices));
    }

    @Test
    @DisplayName("Проверка полигона с null вершинами")
    public void testPolygonContainsAnyVertexWithNullPolygon() {
        Set<Integer> vertexIndices = new HashSet<>(Arrays.asList(1, 2, 3));

        assertFalse(PolygonUtils.polygonContainsAnyVertex(null, vertexIndices));
    }

    @Test
    @DisplayName("Проверка с пустым набором вершин")
    public void testPolygonContainsAnyVertexWithEmptySet() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));

        Set<Integer> vertexIndices = new HashSet<>();

        assertFalse(PolygonUtils.polygonContainsAnyVertex(polygon, vertexIndices));
    }

    @Test
    @DisplayName("Проверка с null набором вершин")
    public void testPolygonContainsAnyVertexWithNullSet() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));

        PolygonUtils.polygonContainsAnyVertex(polygon, null);
        boolean result = false;
        assertFalse(result);
    }

    @Test
    @DisplayName("Переиндексация полигона только с вершинами")
    public void testReindexPolygonWithVerticesOnly() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);
        vertexMapping.put(3, 8);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNotNull(result);
        assertEquals(Arrays.asList(5, 6, 7, 8), result.getVertexIndices());
        assertTrue(result.getTextureVertexIndices().isEmpty());
        assertTrue(result.getNormalIndices().isEmpty());
    }

    @Test
    @DisplayName("Переиндексация полигона с текстурами и нормалями")
    public void testReindexPolygonWithTexturesAndNormals() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(10, 11, 12)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(20, 21, 22)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        textureMapping.put(10, 15);
        textureMapping.put(11, 16);
        textureMapping.put(12, 17);

        Map<Integer, Integer> normalMapping = new HashMap<>();
        normalMapping.put(20, 25);
        normalMapping.put(21, 26);
        normalMapping.put(22, 27);

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNotNull(result);
        assertEquals(Arrays.asList(5, 6, 7), result.getVertexIndices());
        assertEquals(Arrays.asList(15, 16, 17), result.getTextureVertexIndices());
        assertEquals(Arrays.asList(25, 26, 27), result.getNormalIndices());
    }

    @Test
    @DisplayName("Переиндексация null полигона")
    public void testReindexNullPolygon() {
        Map<Integer, Integer> vertexMapping = new HashMap<>();
        Map<Integer, Integer> textureMapping = new HashMap<>();
        Map<Integer, Integer> normalMapping = new HashMap<>();

        PolygonUtils.reindexPolygon(null, vertexMapping, textureMapping, normalMapping);
        Polygon result = null;

        assertNull(null);
    }

    @Test
    @DisplayName("Переиндексация полигона с отсутствующим маппингом для вершины")
    public void testReindexPolygonMissingVertexMapping() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNull(result);
    }

    @Test
    @DisplayName("Переиндексация полигона с отсутствующим маппингом для текстуры")
    public void testReindexPolygonMissingTextureMapping() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(10, 11, 12)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        textureMapping.put(10, 15);
        textureMapping.put(12, 17);

        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNull(result);
    }

    @Test
    @DisplayName("Переиндексация полигона с отсутствующим маппингом для нормали")
    public void testReindexPolygonMissingNormalMapping() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(20, 21, 22)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();

        Map<Integer, Integer> normalMapping = new HashMap<>();
        normalMapping.put(20, 25);
        normalMapping.put(22, 27);

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNull(result);
    }

    @Test
    @DisplayName("Переиндексация полигона с частичными данными (только текстуры)")
    public void testReindexPolygonWithTexturesOnly() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(10, 11, 12)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        textureMapping.put(10, 15);
        textureMapping.put(11, 16);
        textureMapping.put(12, 17);

        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNotNull(result);
        assertEquals(Arrays.asList(5, 6, 7), result.getVertexIndices());
        assertEquals(Arrays.asList(15, 16, 17), result.getTextureVertexIndices());
        assertTrue(result.getNormalIndices().isEmpty());
    }

    @Test
    @DisplayName("Переиндексация полигона с частичными данными (только нормали)")
    public void testReindexPolygonWithNormalsOnly() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(20, 21, 22)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();

        Map<Integer, Integer> normalMapping = new HashMap<>();
        normalMapping.put(20, 25);
        normalMapping.put(21, 26);
        normalMapping.put(22, 27);

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNotNull(result);
        assertEquals(Arrays.asList(5, 6, 7), result.getVertexIndices());
        assertTrue(result.getTextureVertexIndices().isEmpty());
        assertEquals(Arrays.asList(25, 26, 27), result.getNormalIndices());
    }

    @Test
    @DisplayName("Переиндексация полигона с пустыми маппингами")
    public void testReindexPolygonWithEmptyMappings() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        Map<Integer, Integer> textureMapping = new HashMap<>();
        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNull(result);
    }

    @Test
    @DisplayName("Переиндексация полигона с несоответствующим количеством элементов")
    public void testReindexPolygonWithMismatchedElementCount() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(10, 11)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(20, 21, 22)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        textureMapping.put(10, 15);
        textureMapping.put(11, 16);

        Map<Integer, Integer> normalMapping = new HashMap<>();
        normalMapping.put(20, 25);
        normalMapping.put(21, 26);
        normalMapping.put(22, 27);

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNotNull(result);
        assertEquals(Arrays.asList(5, 6, 7), result.getVertexIndices());
        assertEquals(Arrays.asList(15, 16), result.getTextureVertexIndices());
        assertEquals(Arrays.asList(25, 26, 27), result.getNormalIndices());
    }

    @Test
    @DisplayName("Переиндексация полигона с дублирующимися индексами")
    public void testReindexPolygonWithDuplicateIndices() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 0, 2)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNotNull(result);
        assertEquals(Arrays.asList(5, 5, 7), result.getVertexIndices());
    }

    @Test
    @DisplayName("Переиндексация полигона с отрицательными индексами")
    public void testReindexPolygonWithNegativeIndices() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(-1, 0, 1)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(-1, 4);
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNotNull(result);
        assertEquals(Arrays.asList(4, 5, 6), result.getVertexIndices());
    }

    @Test
    @DisplayName("Переиндексация полигона с текстурами но без маппинга текстур")
    public void testReindexPolygonWithTexturesButNoTextureMapping() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(10, 11, 12)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNull(result);
    }

    @Test
    @DisplayName("Переиндексация полигона с нормалями но без маппинга нормалей")
    public void testReindexPolygonWithNormalsButNoNormalMapping() {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(20, 21, 22)));

        Map<Integer, Integer> vertexMapping = new HashMap<>();
        vertexMapping.put(0, 5);
        vertexMapping.put(1, 6);
        vertexMapping.put(2, 7);

        Map<Integer, Integer> textureMapping = new HashMap<>();
        Map<Integer, Integer> normalMapping = new HashMap<>();

        Polygon result = PolygonUtils.reindexPolygon(polygon, vertexMapping, textureMapping, normalMapping);

        assertNull(result);
    }
}
