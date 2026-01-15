package ru.vsu.cs.cg.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    private Model model;
    private Vector3f vertex1;
    private Vector3f vertex2;
    private Vector3f vertex3;
    private Vector2f textureVertex1;
    private Vector2f textureVertex2;
    private Vector3f normal1;
    private Vector3f normal2;
    private Polygon polygon;

    @BeforeEach
    void setUp() {
        model = new Model();

        vertex1 = new Vector3f(0, 0, 0);
        vertex2 = new Vector3f(1, 0, 0);
        vertex3 = new Vector3f(0, 1, 0);

        textureVertex1 = new Vector2f(0, 0);
        textureVertex2 = new Vector2f(1, 0);

        normal1 = new Vector3f(0, 0, 1);
        normal2 = new Vector3f(0, 1, 0);

        List<Integer> vertexIndices = new ArrayList<>(Arrays.asList(0, 1, 2));
        List<Integer> textureIndices = new ArrayList<>(Arrays.asList(0, 1, 0));
        List<Integer> normalIndices = new ArrayList<>(Arrays.asList(0, 1, 0));

        polygon = new Polygon(vertexIndices, textureIndices, normalIndices);
    }

    @Test
    @DisplayName("Конструктор по умолчанию создает пустую модель")
    void testDefaultConstructorCreatesEmptyModel() {
        Model emptyModel = new Model();

        assertNotNull(emptyModel.getVertices());
        assertNotNull(emptyModel.getTextureVertices());
        assertNotNull(emptyModel.getNormals());
        assertNotNull(emptyModel.getPolygons());

        assertTrue(emptyModel.getVertices().isEmpty());
        assertTrue(emptyModel.getTextureVertices().isEmpty());
        assertTrue(emptyModel.getNormals().isEmpty());
        assertTrue(emptyModel.getPolygons().isEmpty());

        assertEquals(1.0f, emptyModel.getMaterialColor()[0]);
        assertEquals(1.0f, emptyModel.getMaterialColor()[1]);
        assertEquals(1.0f, emptyModel.getMaterialColor()[2]);
    }

    @Test
    @DisplayName("Параметризованный конструктор правильно инициализирует модель")
    void testParameterizedConstructorInitializesCorrectly() {
        List<Vector3f> vertices = Arrays.asList(vertex1, vertex2, vertex3);
        List<Vector2f> textureVertices = Arrays.asList(textureVertex1, textureVertex2);
        List<Vector3f> normals = Arrays.asList(normal1, normal2);
        List<Polygon> polygons = Arrays.asList(polygon);

        float[] materialColor = {0.5f, 0.5f, 1.0f};

        Model paramModel = new Model(
                vertices, textureVertices, normals, polygons,
                "TestMaterial", "texture.png", materialColor,
                32.0f, 0.5f, 0.3f
        );

        assertEquals(3, paramModel.getVertices().size());
        assertEquals(2, paramModel.getTextureVertices().size());
        assertEquals(2, paramModel.getNormals().size());
        assertEquals(1, paramModel.getPolygons().size());

        assertEquals("TestMaterial", paramModel.getMaterialName());
        assertEquals("texture.png", paramModel.getTexturePath());
        assertArrayEquals(materialColor, paramModel.getMaterialColor());
        assertEquals(32.0f, paramModel.getMaterialShininess());
        assertEquals(0.5f, paramModel.getMaterialTransparency());
        assertEquals(0.3f, paramModel.getMaterialReflectivity());
    }

    @Test
    @DisplayName("Добавление вершины работает корректно")
    void testAddVertex() {
        model.addVertex(vertex1);
        model.addVertex(vertex2);

        List<Vector3f> vertices = model.getVertices();
        assertEquals(2, vertices.size());
        assertEquals(vertex1, vertices.get(0));
        assertEquals(vertex2, vertices.get(1));
    }

    @Test
    @DisplayName("Добавление текстурной вершины работает корректно")
    void testAddTextureVertex() {
        model.addTextureVertex(textureVertex1);
        model.addTextureVertex(textureVertex2);

        List<Vector2f> textureVertices = model.getTextureVertices();
        assertEquals(2, textureVertices.size());
        assertEquals(textureVertex1, textureVertices.get(0));
        assertEquals(textureVertex2, textureVertices.get(1));
    }

    @Test
    @DisplayName("Добавление нормали работает корректно")
    void testAddNormal() {
        model.addNormal(normal1);
        model.addNormal(normal2);

        List<Vector3f> normals = model.getNormals();
        assertEquals(2, normals.size());
        assertEquals(normal1, normals.get(0));
        assertEquals(normal2, normals.get(1));
    }

    @Test
    @DisplayName("Добавление полигона работает корректно")
    void testAddPolygon() {
        model.addVertex(vertex1);
        model.addVertex(vertex2);
        model.addVertex(vertex3);
        model.addTextureVertex(textureVertex1);
        model.addTextureVertex(textureVertex2);
        model.addNormal(normal1);
        model.addNormal(normal2);

        model.addPolygon(polygon);

        List<Polygon> polygons = model.getPolygons();
        assertEquals(1, polygons.size());
        assertEquals(polygon, polygons.get(0));
    }

    @Test
    @DisplayName("Очистка полигонов работает корректно")
    void testClearPolygons() {
        model.addPolygon(polygon);
        assertFalse(model.getPolygons().isEmpty());

        model.clearPolygons();
        assertTrue(model.getPolygons().isEmpty());
    }

    @Test
    @DisplayName("Добавление всех полигонов работает корректно")
    void testAddAllPolygons() {
        Polygon polygon2 = new Polygon(
                Arrays.asList(0, 2, 3),
                Arrays.asList(0, 1, 0),
                Arrays.asList(0, 1, 0)
        );

        List<Polygon> newPolygons = Arrays.asList(polygon, polygon2);

        model.addAllPolygons(newPolygons);

        List<Polygon> polygons = model.getPolygons();
        assertEquals(2, polygons.size());
        assertEquals(polygon, polygons.get(0));
        assertEquals(polygon2, polygons.get(1));
    }

    @Test
    @DisplayName("Установка вершин работает корректно")
    void testSetVertices() {
        List<Vector3f> newVertices = Arrays.asList(vertex1, vertex2, vertex3);

        model.setVertices(newVertices);

        List<Vector3f> vertices = model.getVertices();
        assertEquals(3, vertices.size());
        assertEquals(newVertices, vertices);
    }

    @Test
    @DisplayName("Установка текстурных вершин работает корректно")
    void testSetTextureVertices() {
        List<Vector2f> newTextureVertices = Arrays.asList(textureVertex1, textureVertex2);

        model.setTextureVertices(newTextureVertices);

        List<Vector2f> textureVertices = model.getTextureVertices();
        assertEquals(2, textureVertices.size());
        assertEquals(newTextureVertices, textureVertices);
    }

    @Test
    @DisplayName("Установка нормалей работает корректно")
    void testSetNormals() {
        List<Vector3f> newNormals = Arrays.asList(normal1, normal2);

        model.setNormals(newNormals);

        List<Vector3f> normals = model.getNormals();
        assertEquals(2, normals.size());
        assertEquals(newNormals, normals);
    }

    @Test
    @DisplayName("Установка полигонов работает корректно")
    void testSetPolygons() {
        List<Polygon> newPolygons = Arrays.asList(polygon);

        model.setPolygons(newPolygons);

        List<Polygon> polygons = model.getPolygons();
        assertEquals(1, polygons.size());
        assertEquals(newPolygons, polygons);
    }

    @Test
    @DisplayName("Кэш триангулированных полигонов работает корректно")
    void testTriangulatedPolygonsCache() {
        model.addVertex(vertex1);
        model.addVertex(vertex2);
        model.addVertex(vertex3);
        model.addPolygon(polygon);

        List<Polygon> triangulated1 = model.getTriangulatedPolygonsCache();
        assertNotNull(triangulated1);
        assertFalse(triangulated1.isEmpty());

        List<Polygon> triangulated2 = model.getTriangulatedPolygonsCache();
        assertSame(triangulated1, triangulated2, "Должен возвращаться кэшированный результат");

        model.addVertex(new Vector3f(1, 1, 0));
        List<Polygon> triangulated3 = model.getTriangulatedPolygonsCache();
        assertNotSame(triangulated1, triangulated3, "Кэш должен инвалидироваться после изменения");
    }
}