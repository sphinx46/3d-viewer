package ru.vsu.cs.cg.model;

import ru.vsu.cs.cg.math.NormalCalculator;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.render_engine.GraphicConveyor;
import java.util.*;

public final class Model {
    private final List<Vector3f> vertices;
    private final List<Vector2f> textureVertices;
    private final List<Vector3f> normals;
    private final List<Polygon> polygons;
    private Vector3f translation = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private Vector3f scale = new Vector3f(1, 1, 1);

    private volatile List<Polygon> triangulatedPolygonsCache = null;

    /**
     * Создает пустую модель
     */
    public Model() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Создает модель из существующих данных
     */
    private Model(List<Vector3f> vertices,
                  List<Vector2f> textureVertices,
                  List<Vector3f> normals,
                  List<Polygon> polygons) {
        this.vertices = new ArrayList<>(Objects.requireNonNull(vertices, "Вершины не могут быть null"));
        this.textureVertices = new ArrayList<>(Objects.requireNonNull(textureVertices, "Текстурные вершины не могут быть null"));
        this.normals = new ArrayList<>(Objects.requireNonNull(normals, "Нормали не могут быть null"));
        this.polygons = new ArrayList<>(Objects.requireNonNull(polygons, "Полигоны не могут быть null"));
    }

    /**
     * Добавляет вершину в модель
     */
    public void addVertex(Vector3f vertex) {
        vertices.add(vertex);
        invalidateTriangulation();
    }

    /**
     * Очищает все полигоны модели
     */
    public void clearPolygons() {
        polygons.clear();
        invalidateTriangulation();
    }

    /**
     * Добавляет коллекцию полигонов в модель
     */
    public void addAllPolygons(Collection<Polygon> newPolygons) {
        if (newPolygons != null && !newPolygons.isEmpty()) {
            polygons.addAll(newPolygons);
            invalidateTriangulation();
        }
    }

    /**
     * Добавляет текстурную вершину в модель
     */
    public void addTextureVertex(Vector2f textureVertex) {
        textureVertices.add(textureVertex);
    }

    /**
     * Заменяет все вершины модели новым списком
     */
    public void setVertices(List<Vector3f> vertices) {
        this.vertices.clear();
        if (vertices != null) {
            this.vertices.addAll(vertices);
        }
        invalidateTriangulation();
    }

    /**
     * Заменяет все текстурные вершины модели новым списком
     */
    public void setTextureVertices(List<Vector2f> textureVertices) {
        this.textureVertices.clear();
        if (textureVertices != null) {
            this.textureVertices.addAll(textureVertices);
        }
    }

    /**
     * Заменяет все нормали модели новым списком
     */
    public void setNormals(List<Vector3f> normals) {
        this.normals.clear();
        if (normals != null) {
            this.normals.addAll(normals);
        }
    }

    /**
     * Заменяет все полигоны модели новым списком
     */
    public void setPolygons(List<Polygon> polygons) {
        this.polygons.clear();
        if (polygons != null) {
            this.polygons.addAll(polygons);
        }
        invalidateTriangulation();
    }

    /**
     * Добавляет нормаль в модель
     */
    public void addNormal(Vector3f normal) {
        normals.add(normal);
    }

    /**
     * Добавляет полигон в модель
     */
    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
        invalidateTriangulation();
    }

    /**
     * Возвращает полигоны после триангуляции
     * при необходимости пересчитывает их
     * @return полигоны после триангуляции
     */
    public List<Polygon> getTriangulatedPolygonsCache() {
        List<Polygon> cache = triangulatedPolygonsCache;

        if (cache == null) {
            cache = computeTriangulation();
            triangulatedPolygonsCache = cache;
        }

        return cache;
    }

    /**
     * Вычисляет триангуляцию для всех полигонов модели
     */
    private List<Polygon> computeTriangulation() {
        if (polygons.isEmpty()) {
            return Collections.emptyList();
        }

        List<Polygon> triangulatedPolygons = new ArrayList<>();
        for (Polygon polygon : polygons) {
            triangulatedPolygons.addAll(polygon.triangulate());
        }
        return Collections.unmodifiableList(triangulatedPolygons);
    }

    /**
     * Пересчитывает нормали вершин модели
     */
    public void recomputeNormals() {
        List<Vector3f> newNormals = NormalCalculator.computeVertexNormals(vertices, polygons);
        normals.clear();
        normals.addAll(newNormals);

        for (Polygon polygon : polygons) {
            List<Integer> normalIndices = polygon.getNormalIndices();
            if (normalIndices.isEmpty()) {
                normalIndices.addAll(polygon.getVertexIndices());
            }
        }
    }

    /**
     * Помечает кэш триангуляции как невалидный
     */
    private void invalidateTriangulation() {
        triangulatedPolygonsCache = null;
    }

    /**
     * Возвращает неизменяемый список вершин модели
     */
    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    /**
     * Возвращает неизменяемый список текстурных вершин модели
     */
    public List<Vector2f> getTextureVertices() {
        return Collections.unmodifiableList(textureVertices);
    }

    /**
     * Возвращает неизменяемый список нормалей модели
     */
    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    /**
     * Возвращает неизменяемый список полигонов модели
     */
    public List<Polygon> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }

    /**
     * Возвращает изменяемый список вершин модели
     * Для операций, требующих модификации вершин
     */
    public List<Vector3f> getVerticesMutable() {
        return vertices;
    }

    /**
     * Возвращает изменяемый список полигонов модели
     * Для операций, требующих модификации полигонов
     */
    public List<Polygon> getPolygonsMutable() {
        return polygons;
    }

    /**
     * Возвращает изменяемый список текстурных вершин модели
     */
    public List<Vector2f> getTextureVerticesMutable() {
        return textureVertices;
    }

    /**
     * Возвращает изменяемый список нормалей модели
     */
    public List<Vector3f> getNormalsMutable() {
        return normals;
    }

    /**
     * Создает копию модели с текущим состоянием
     */
    public Model copy() {
        return new Model(vertices, textureVertices, normals, polygons);
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public Vector3f getScale() {
        return scale;
    }

    public List<Vector3f> getTransformedVertices() {
        List<Vector3f> transformedVertices = new ArrayList<>(vertices.size());

        Matrix4x4 modelMatrix = GraphicConveyor.rotateScaleTranslate(translation, rotation, scale);

        for (Vector3f vertex : vertices) {
            Vector3f transformed = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, vertex);
            transformedVertices.add(transformed);
        }

        return transformedVertices;
    }
}
