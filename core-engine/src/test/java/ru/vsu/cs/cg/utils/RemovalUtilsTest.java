package ru.vsu.cs.cg.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.objreader.ObjReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RemovalUtilsTest {

    private final static String BASE_TEST_RESOURCE_PATH = "src/test/resources";
    private Model simpleCubeModel;

    @BeforeEach
    void setUp() throws IOException {
        String simpleCubeContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH + "/simpleCube.obj"));
        simpleCubeModel = ObjReader.read(simpleCubeContent);
    }

    @Test
    @DisplayName("Удаление элементов по индексам из списка")
    public void testRemoveElementsByIndices() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        Set<Integer> indices = new HashSet<>(Arrays.asList(1, 3));

        RemovalUtils.removeElementsByIndices(list, indices);

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
        assertEquals("E", list.get(2));
    }

    @Test
    @DisplayName("Удаление элементов с null набором индексов")
    public void testRemoveElementsByIndicesWithNullSet() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));

        RemovalUtils.removeElementsByIndices(list, null);

        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("Удаление элементов с пустым набором индексов")
    public void testRemoveElementsByIndicesWithEmptySet() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
        Set<Integer> indices = new HashSet<>();

        RemovalUtils.removeElementsByIndices(list, indices);

        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("Удаление элементов с невалидными индексами")
    public void testRemoveElementsByIndicesWithInvalidIndices() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
        Set<Integer> indices = new HashSet<>(Arrays.asList(-1, 5, 10));

        RemovalUtils.removeElementsByIndices(list, indices);

        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("Удаление элементов с обратным порядком индексов")
    public void testRemoveElementsByIndicesReverseOrder() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        Set<Integer> indices = new HashSet<>(Arrays.asList(0, 2, 4));

        RemovalUtils.removeElementsByIndices(list, indices);

        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("D", list.get(1));
    }

    @Test
    @DisplayName("Удаление вершин из модели")
    public void testRemoveVerticesFromModel() {
        int initialVertexCount = simpleCubeModel.vertices.size();
        Set<Integer> verticesToRemove = new HashSet<>(Arrays.asList(0, 1));

        RemovalUtils.removeVerticesFromModel(simpleCubeModel, verticesToRemove);

        assertEquals(initialVertexCount - 2, simpleCubeModel.vertices.size());
    }

    @Test
    @DisplayName("Удаление вершин из модели с null набором индексов")
    public void testRemoveVerticesFromModelWithNullSet() {
        int initialVertexCount = simpleCubeModel.vertices.size();

        RemovalUtils.removeVerticesFromModel(simpleCubeModel, null);

        assertEquals(initialVertexCount, simpleCubeModel.vertices.size());
    }

    @Test
    @DisplayName("Удаление вершин из модели с пустым набором индексов")
    public void testRemoveVerticesFromModelWithEmptySet() {
        int initialVertexCount = simpleCubeModel.vertices.size();
        Set<Integer> verticesToRemove = new HashSet<>();

        RemovalUtils.removeVerticesFromModel(simpleCubeModel, verticesToRemove);

        assertEquals(initialVertexCount, simpleCubeModel.vertices.size());
    }

    @Test
    @DisplayName("Удаление полигонов из модели")
    public void testRemovePolygonsFromModel() {
        int initialPolygonCount = simpleCubeModel.polygons.size();
        Set<Integer> polygonsToRemove = new HashSet<>(Arrays.asList(0, 1));

        RemovalUtils.removePolygonsFromModel(simpleCubeModel, polygonsToRemove);

        assertEquals(initialPolygonCount - 2, simpleCubeModel.polygons.size());
    }

    @Test
    @DisplayName("Удаление полигонов из модели с null набором индексов")
    public void testRemovePolygonsFromModelWithNullSet() {
        int initialPolygonCount = simpleCubeModel.polygons.size();

        RemovalUtils.removePolygonsFromModel(simpleCubeModel, null);

        assertEquals(initialPolygonCount, simpleCubeModel.polygons.size());
    }

    @Test
    @DisplayName("Удаление полигонов из модели с пустым набором индексов")
    public void testRemovePolygonsFromModelWithEmptySet() {
        int initialPolygonCount = simpleCubeModel.polygons.size();
        Set<Integer> polygonsToRemove = new HashSet<>();

        RemovalUtils.removePolygonsFromModel(simpleCubeModel, polygonsToRemove);

        assertEquals(initialPolygonCount, simpleCubeModel.polygons.size());
    }

    @Test
    @DisplayName("Удаление неиспользуемых текстурных координат")
    public void testRemoveUnusedTextureVertices() {
        Model testModel = new Model();
        testModel.textureVertices = new ArrayList<>(Arrays.asList(
                new Vector2f(0.0f, 0.0f),
                new Vector2f(1.0f, 0.0f),
                new Vector2f(1.0f, 1.0f),
                new Vector2f(0.0f, 1.0f)
        ));

        Set<Integer> usedTextureIndices = new HashSet<>(Arrays.asList(0, 2));

        RemovalUtils.removeUnusedTextureVertices(testModel, usedTextureIndices);

        assertEquals(2, testModel.textureVertices.size());
    }

    @Test
    @DisplayName("Удаление всех текстурных координат при null наборе используемых индексов")
    public void testRemoveUnusedTextureVerticesWithNullUsedIndices() {
        Model testModel = new Model();
        testModel.textureVertices = new ArrayList<>(Arrays.asList(
                new Vector2f(0.0f, 0.0f),
                new Vector2f(1.0f, 0.0f)
        ));

        RemovalUtils.removeUnusedTextureVertices(testModel, null);

        assertEquals(0, testModel.textureVertices.size());
    }

    @Test
    @DisplayName("Удаление всех текстурных координат при пустом наборе используемых индексов")
    public void testRemoveUnusedTextureVerticesWithEmptyUsedIndices() {
        Model testModel = new Model();
        testModel.textureVertices = new ArrayList<>(Arrays.asList(
                new Vector2f(0.0f, 0.0f),
                new Vector2f(1.0f, 0.0f)
        ));

        Set<Integer> usedTextureIndices = new HashSet<>();

        RemovalUtils.removeUnusedTextureVertices(testModel, usedTextureIndices);

        assertEquals(0, testModel.textureVertices.size());
    }

    @Test
    @DisplayName("Удаление неиспользуемых нормалей")
    public void testRemoveUnusedNormals() {
        Model testModel = new Model();
        testModel.normals = new ArrayList<>(Arrays.asList(
                new Vector3f(0.0f, 0.0f, 1.0f),
                new Vector3f(0.0f, 1.0f, 0.0f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, -1.0f)
        ));

        Set<Integer> usedNormalIndices = new HashSet<>(Arrays.asList(1, 3));

        RemovalUtils.removeUnusedNormals(testModel, usedNormalIndices);

        assertEquals(2, testModel.normals.size());
    }

    @Test
    @DisplayName("Удаление всех нормалей при null наборе используемых индексов")
    public void testRemoveUnusedNormalsWithNullUsedIndices() {
        Model testModel = new Model();
        testModel.normals = new ArrayList<>(Arrays.asList(
                new Vector3f(0.0f, 0.0f, 1.0f),
                new Vector3f(0.0f, 1.0f, 0.0f)
        ));

        RemovalUtils.removeUnusedNormals(testModel, null);

        assertEquals(0, testModel.normals.size());
    }

    @Test
    @DisplayName("Удаление всех нормалей при пустом наборе используемых индексов")
    public void testRemoveUnusedNormalsWithEmptyUsedIndices() {
        Model testModel = new Model();
        testModel.normals = new ArrayList<>(Arrays.asList(
                new Vector3f(0.0f, 0.0f, 1.0f),
                new Vector3f(0.0f, 1.0f, 0.0f)
        ));

        Set<Integer> usedNormalIndices = new HashSet<>();

        RemovalUtils.removeUnusedNormals(testModel, usedNormalIndices);

        assertEquals(0, testModel.normals.size());
    }

    @Test
    @DisplayName("Удаление неиспользуемых текстурных координат из пустого списка")
    public void testRemoveUnusedTextureVerticesFromEmptyList() {
        Model testModel = new Model();
        testModel.textureVertices = new ArrayList<>();

        Set<Integer> usedTextureIndices = new HashSet<>(Arrays.asList(0, 1));

        RemovalUtils.removeUnusedTextureVertices(testModel, usedTextureIndices);

        assertEquals(0, testModel.textureVertices.size());
    }

    @Test
    @DisplayName("Удаление неиспользуемых нормалей из пустого списка")
    public void testRemoveUnusedNormalsFromEmptyList() {
        Model testModel = new Model();
        testModel.normals = new ArrayList<>();

        Set<Integer> usedNormalIndices = new HashSet<>(Arrays.asList(0, 1));

        RemovalUtils.removeUnusedNormals(testModel, usedNormalIndices);

        assertEquals(0, testModel.normals.size());
    }

    @Test
    @DisplayName("Удаление неиспользуемых текстурных координат с невалидными индексами")
    public void testRemoveUnusedTextureVerticesWithInvalidIndices() {
        Model testModel = new Model();
        testModel.textureVertices = new ArrayList<>(Arrays.asList(
                new Vector2f(0.0f, 0.0f),
                new Vector2f(1.0f, 0.0f)
        ));

        Set<Integer> usedTextureIndices = new HashSet<>(Arrays.asList(-1, 5, 10));

        RemovalUtils.removeUnusedTextureVertices(testModel, usedTextureIndices);

        assertEquals(0, testModel.textureVertices.size());
    }

    @Test
    @DisplayName("Удаление неиспользуемых нормалей с невалидными индексами")
    public void testRemoveUnusedNormalsWithInvalidIndices() {
        Model testModel = new Model();
        testModel.normals = new ArrayList<>(Arrays.asList(
                new Vector3f(0.0f, 0.0f, 1.0f),
                new Vector3f(0.0f, 1.0f, 0.0f)
        ));

        Set<Integer> usedNormalIndices = new HashSet<>(Arrays.asList(-1, 5, 10));

        RemovalUtils.removeUnusedNormals(testModel, usedNormalIndices);

        assertEquals(0, testModel.normals.size());
    }
}