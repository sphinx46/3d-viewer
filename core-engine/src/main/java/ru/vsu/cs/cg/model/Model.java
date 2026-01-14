package ru.vsu.cs.cg.model;

import ru.vsu.cs.cg.math.NormalCalculator;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.renderEngine.GraphicConveyor;
import java.util.*;

public final class Model {
    private final List<Vector3f> vertices;
    private final List<Vector2f> textureVertices;
    private final List<Vector3f> normals;
    private final List<Polygon> polygons;
    private volatile List<Polygon> triangulatedPolygonsCache = null;

    public Model() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private Model(List<Vector3f> vertices,
                  List<Vector2f> textureVertices,
                  List<Vector3f> normals,
                  List<Polygon> polygons) {
        this.vertices = new ArrayList<>(Objects.requireNonNull(vertices));
        this.textureVertices = new ArrayList<>(Objects.requireNonNull(textureVertices));
        this.normals = new ArrayList<>(Objects.requireNonNull(normals));
        this.polygons = new ArrayList<>(Objects.requireNonNull(polygons));
    }

    public void addVertex(Vector3f vertex) {
        vertices.add(vertex);
        invalidateTriangulation();
    }

    public void clearPolygons() {
        polygons.clear();
        invalidateTriangulation();
    }

    public void addAllPolygons(Collection<Polygon> newPolygons) {
        if (newPolygons != null && !newPolygons.isEmpty()) {
            polygons.addAll(newPolygons);
            invalidateTriangulation();
        }
    }

    public void addTextureVertex(Vector2f textureVertex) {
        textureVertices.add(textureVertex);
    }

    public void setVertices(List<Vector3f> vertices) {
        this.vertices.clear();
        if (vertices != null) {
            this.vertices.addAll(vertices);
        }
        invalidateTriangulation();
    }

    public void setTextureVertices(List<Vector2f> textureVertices) {
        this.textureVertices.clear();
        if (textureVertices != null) {
            this.textureVertices.addAll(textureVertices);
        }
    }

    public void setNormals(List<Vector3f> normals) {
        this.normals.clear();
        if (normals != null) {
            this.normals.addAll(normals);
        }
    }

    public void setPolygons(List<Polygon> polygons) {
        this.polygons.clear();
        if (polygons != null) {
            this.polygons.addAll(polygons);
        }
        invalidateTriangulation();
    }

    public void addNormal(Vector3f normal) {
        normals.add(normal);
    }

    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
        invalidateTriangulation();
    }

    public List<Polygon> getTriangulatedPolygonsCache() {
        List<Polygon> cache = triangulatedPolygonsCache;

        if (cache == null) {
            cache = computeTriangulation();
            triangulatedPolygonsCache = cache;
        }

        return cache;
    }

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

    public void recomputeNormals() {
        List<Vector3f> newNormals = NormalCalculator.computeVertexNormals(vertices, polygons);
        normals.clear();
        normals.addAll(newNormals);

        for (Polygon polygon : polygons) {
            List<Integer> normalIndices = polygon.getNormalIndices();
            normalIndices.clear();
            normalIndices.addAll(polygon.getVertexIndices());
        }
    }

    private void invalidateTriangulation() {
        triangulatedPolygonsCache = null;
    }

    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Vector2f> getTextureVertices() {
        return Collections.unmodifiableList(textureVertices);
    }

    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    public List<Polygon> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }

    public List<Vector3f> getVerticesMutable() {
        return vertices;
    }

    public List<Polygon> getPolygonsMutable() {
        return polygons;
    }

    public List<Vector2f> getTextureVerticesMutable() {
        return textureVertices;
    }

    public List<Vector3f> getNormalsMutable() {
        return normals;
    }

    public Model copy() {
        return new Model(vertices, textureVertices, normals, polygons);
    }

    public List<Vector3f> getTransformedVertices(Vector3f translation, Vector3f rotation, Vector3f scale) {
        List<Vector3f> transformedVertices = new ArrayList<>(vertices.size());

        Matrix4x4 modelMatrix = GraphicConveyor.rotateScaleTranslate(translation, rotation, scale);

        for (Vector3f vertex : vertices) {
            Vector3f transformed = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, vertex);
            transformedVertices.add(transformed);
        }

        return transformedVertices;
    }

    public Model createTransformedCopy(Vector3f translation, Vector3f rotation, Vector3f scale) {
        List<Vector3f> transformedVertices = getTransformedVertices(translation, rotation, scale);

        return new Model(
            transformedVertices,
            new ArrayList<>(textureVertices),
            new ArrayList<>(normals),
            new ArrayList<>(polygons)
        );
    }
}
