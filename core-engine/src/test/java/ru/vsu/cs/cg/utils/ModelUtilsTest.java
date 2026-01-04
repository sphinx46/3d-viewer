package ru.vsu.cs.cg.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.objreader.ObjReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ModelUtilsTest {

    private final static String BASE_TEST_RESOURCE_PATH = "src/test/resources";
    private Model simpleCubeModel;
    private Model cubeWithAdditionalVModel;

    @BeforeEach
    void setUp() throws IOException {
        String simpleCubeContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH + "/simpleCube.obj"));
        simpleCubeModel = ObjReader.read(simpleCubeContent);

        String cubeWithAdditionalVContent = Files.readString(Paths.get(BASE_TEST_RESOURCE_PATH + "/cubeWithAdditionalV.obj"));
        cubeWithAdditionalVModel = ObjReader.read(cubeWithAdditionalVContent);
    }

    @Test
    @DisplayName("Поиск полигонов, содержащих указанные вершины")
    public void testFindPolygonsContainingVertices() {
        Set<Integer> vertexIndices = new HashSet<>(Arrays.asList(0, 1));
        Set<Integer> polygons = ModelUtils.findPolygonsContainingVertices(simpleCubeModel, vertexIndices);

        assertNotNull(polygons);
        assertEquals(4, polygons.size());
        assertTrue(polygons.contains(0));
        assertTrue(polygons.contains(2));
        assertTrue(polygons.contains(3));
        assertTrue(polygons.contains(5));
    }

    @Test
    @DisplayName("Поиск полигонов с пустым набором вершин")
    public void testFindPolygonsContainingVerticesEmptySet() {
        Set<Integer> vertexIndices = new HashSet<>();
        Set<Integer> polygons = ModelUtils.findPolygonsContainingVertices(simpleCubeModel, vertexIndices);

        assertNotNull(polygons);
        assertEquals(0, polygons.size());
    }

    @Test
    @DisplayName("Поиск полигонов с несуществующими вершинами")
    public void testFindPolygonsContainingVerticesNonExistent() {
        Set<Integer> vertexIndices = new HashSet<>(Arrays.asList(100, 200));
        Set<Integer> polygons = ModelUtils.findPolygonsContainingVertices(simpleCubeModel, vertexIndices);

        assertNotNull(polygons);
        assertEquals(0, polygons.size());
    }

    @Test
    @DisplayName("Сбор вершин из указанных полигонов")
    public void testCollectVerticesFromPolygons() {
        Set<Integer> polygonIndices = new HashSet<>(Arrays.asList(0, 1));
        Set<Integer> vertices = ModelUtils.collectVerticesFromPolygons(simpleCubeModel, polygonIndices);

        assertNotNull(vertices);
        assertEquals(8, vertices.size());
        for (int i = 0; i < 8; i++) {
            assertTrue(vertices.contains(i));
        }
    }

    @Test
    @DisplayName("Сбор текстурных индексов из указанных полигонов")
    public void testCollectTextureIndicesFromPolygons() {
        Set<Integer> polygonIndices = new HashSet<>(Arrays.asList(0, 1));
        Set<Integer> textureIndices = ModelUtils.collectTextureIndicesFromPolygons(cubeWithAdditionalVModel, polygonIndices);

        assertNotNull(textureIndices);
        assertTrue(textureIndices.size() > 0);
    }

    @Test
    @DisplayName("Сбор нормалей из указанных полигонов")
    public void testCollectNormalIndicesFromPolygons() {
        Set<Integer> polygonIndices = new HashSet<>(Arrays.asList(0, 1));
        Set<Integer> normalIndices = ModelUtils.collectNormalIndicesFromPolygons(cubeWithAdditionalVModel, polygonIndices);

        assertNotNull(normalIndices);
        assertTrue(normalIndices.size() > 0);
    }

    @Test
    @DisplayName("Сбор используемых вершин из модели")
    public void testCollectUsedVertices() {
        Set<Integer> usedVertices = ModelUtils.collectUsedVertices(simpleCubeModel);

        assertNotNull(usedVertices);
        assertEquals(8, usedVertices.size());
        for (int i = 0; i < 8; i++) {
            assertTrue(usedVertices.contains(i));
        }
    }

    @Test
    @DisplayName("Сбор используемых текстурных индексов из модели")
    public void testCollectUsedTextureIndices() {
        Set<Integer> usedTextureIndices = ModelUtils.collectUsedTextureIndices(cubeWithAdditionalVModel);

        assertNotNull(usedTextureIndices);
        assertTrue(usedTextureIndices.size() > 0);
    }

    @Test
    @DisplayName("Сбор используемых нормалей из модели")
    public void testCollectUsedNormalIndices() {
        Set<Integer> usedNormalIndices = ModelUtils.collectUsedNormalIndices(cubeWithAdditionalVModel);

        assertNotNull(usedNormalIndices);
        assertTrue(usedNormalIndices.size() > 0);
    }

    @Test
    @DisplayName("Удаление вершин из модели")
    public void testRemoveVerticesFromModel() {
        int initialVertexCount = simpleCubeModel.vertices.size();
        Set<Integer> verticesToRemove = new HashSet<>(Arrays.asList(0, 1));

        ModelUtils.removeVerticesFromModel(simpleCubeModel, verticesToRemove);

        assertEquals(initialVertexCount - 2, simpleCubeModel.vertices.size());
    }

    @Test
    @DisplayName("Удаление полигонов из модели")
    public void testRemovePolygonsFromModel() {
        int initialPolygonCount = simpleCubeModel.polygons.size();
        Set<Integer> polygonsToRemove = new HashSet<>(Arrays.asList(0, 1));

        ModelUtils.removePolygonsFromModel(simpleCubeModel, polygonsToRemove);

        assertEquals(initialPolygonCount - 2, simpleCubeModel.polygons.size());
    }

    @Test
    @DisplayName("Проверка валидности индекса вершины")
    public void testIsValidVertexIndex() {
        assertTrue(ModelUtils.isValidVertexIndex(simpleCubeModel, 0));
        assertTrue(ModelUtils.isValidVertexIndex(simpleCubeModel, 7));
        assertFalse(ModelUtils.isValidVertexIndex(simpleCubeModel, -1));
        assertFalse(ModelUtils.isValidVertexIndex(simpleCubeModel, 8));
        assertFalse(ModelUtils.isValidVertexIndex(simpleCubeModel, 100));
    }

    @Test
    @DisplayName("Частичная переиндексация модели")
    public void testPartialReindexModel() {
        Model testModel = createSimpleTestModel();

        Set<Integer> removedVertexIndices = new HashSet<>(Arrays.asList(0));
        Set<Integer> removedTextureIndices = new HashSet<>();
        Set<Integer> removedNormalIndices = new HashSet<>();

        int initialPolygonCount = testModel.polygons.size();

        ModelUtils.partialReindexModel(testModel, removedVertexIndices,
                removedTextureIndices, removedNormalIndices);

        assertTrue(testModel.polygons.size() > 0);

        for (Polygon polygon : testModel.polygons) {
            for (Integer vertexIndex : polygon.getVertexIndices()) {
                assertTrue(vertexIndex >= 0);
            }
        }
    }

    @Test
    @DisplayName("Полная переиндексация модели")
    public void testFullReindexModel() {
        Model testModel = createSimpleTestModel();

        Set<Integer> removedVertexIndices = new HashSet<>(Arrays.asList(0));
        Set<Integer> removedTextureIndices = new HashSet<>();
        Set<Integer> removedNormalIndices = new HashSet<>();

        int initialPolygonCount = testModel.polygons.size();

        ModelUtils.fullReindexModel(testModel, removedVertexIndices,
                removedTextureIndices, removedNormalIndices);

        assertTrue(testModel.polygons.size() > 0);

        Set<Integer> usedIndices = new HashSet<>();
        for (Polygon polygon : testModel.polygons) {
            usedIndices.addAll(polygon.getVertexIndices());
        }

        assertTrue(usedIndices.size() > 0);
        for (Integer index : usedIndices) {
            assertTrue(index >= 0);
        }
    }

    @Test
    @DisplayName("Частичная переиндексация пустой модели")
    public void testPartialReindexModelEmptyModel() {
        Model emptyModel = new Model();
        emptyModel.polygons = new ArrayList<>();
        Set<Integer> removedVertexIndices = new HashSet<>(Arrays.asList(0, 1));
        Set<Integer> removedTextureIndices = new HashSet<>();
        Set<Integer> removedNormalIndices = new HashSet<>();

        assertDoesNotThrow(() -> {
            ModelUtils.partialReindexModel(emptyModel, removedVertexIndices,
                    removedTextureIndices, removedNormalIndices);
        });
    }

    @Test
    @DisplayName("Полная переиндексация пустой модели")
    public void testFullReindexModelEmptyModel() {
        Model emptyModel = new Model();
        emptyModel.polygons = new ArrayList<>();
        Set<Integer> removedVertexIndices = new HashSet<>(Arrays.asList(0, 1));
        Set<Integer> removedTextureIndices = new HashSet<>();
        Set<Integer> removedNormalIndices = new HashSet<>();

        assertDoesNotThrow(() -> {
            ModelUtils.fullReindexModel(emptyModel, removedVertexIndices,
                    removedTextureIndices, removedNormalIndices);
        });
    }

    @Test
    @DisplayName("Сбор данных из невалидных индексов полигонов")
    public void testCollectDataFromInvalidPolygonIndices() {
        Model model = new Model();
        model.polygons = new ArrayList<>();

        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.polygons.add(polygon1);

        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        model.polygons.add(polygon2);

        Set<Integer> invalidIndices = new HashSet<>(Arrays.asList(10, 20, -1));
        Set<Integer> vertices = ModelUtils.collectVerticesFromPolygons(model, invalidIndices);

        assertNotNull(vertices);
        assertEquals(0, vertices.size());
    }

    @Test
    @DisplayName("Частичная переиндексация после удаления полигонов")
    public void testPartialReindexModelAfterRemovingPolygons() {
        Model testModel = new Model();
        testModel.polygons = new ArrayList<>();

        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        testModel.polygons.add(polygon1);

        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        testModel.polygons.add(polygon2);

        Set<Integer> removedVertexIndices = new HashSet<>(Arrays.asList(0));
        Set<Integer> removedTextureIndices = new HashSet<>();
        Set<Integer> removedNormalIndices = new HashSet<>();

        ModelUtils.partialReindexModel(testModel, removedVertexIndices,
                removedTextureIndices, removedNormalIndices);

        assertTrue(testModel.polygons.size() >= 1);

        for (Polygon polygon : testModel.polygons) {
            for (Integer vertexIndex : polygon.getVertexIndices()) {
                assertTrue(vertexIndex >= 0);
            }
        }
    }

    @Test
    @DisplayName("Сбор данных из полигона с текстурами и нормалями")
    public void testCollectDataFromPolygonWithTexturesAndNormals() {
        Set<Integer> polygonIndices = new HashSet<>(Arrays.asList(0));
        Set<Integer> vertices = ModelUtils.collectVerticesFromPolygons(cubeWithAdditionalVModel, polygonIndices);
        Set<Integer> textures = ModelUtils.collectTextureIndicesFromPolygons(cubeWithAdditionalVModel, polygonIndices);
        Set<Integer> normals = ModelUtils.collectNormalIndicesFromPolygons(cubeWithAdditionalVModel, polygonIndices);

        assertNotNull(vertices);
        assertNotNull(textures);
        assertNotNull(normals);
        assertTrue(vertices.size() > 0);
        assertTrue(textures.size() > 0);
        assertTrue(normals.size() > 0);
    }

    private Model createSimpleTestModel() {
        Model model = new Model();
        model.polygons = new ArrayList<>();

        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        Polygon polygon3 = new Polygon();
        polygon3.setVertexIndices(new ArrayList<>(Arrays.asList(2, 3, 4)));

        model.polygons.add(polygon1);
        model.polygons.add(polygon2);
        model.polygons.add(polygon3);

        return model;
    }
}