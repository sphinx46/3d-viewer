package ru.vsu.cs.cg.model;

import ru.vsu.cs.cg.math.NormalCalculator;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.renderEngine.GraphicConveyor;
import ru.vsu.cs.cg.model.selection.ModelSelection;

import java.util.*;

public final class Model {
    private final List<Vector3f> vertices;
    private final List<Vector2f> textureVertices;
    private final List<Vector3f> normals;
    private final List<Polygon> polygons;
    private boolean useLighting = false;
    private boolean useTexture = false;
    private boolean drawPolygonalGrid = false;
    private volatile List<Polygon> triangulatedPolygonsCache = null;
    private final ModelSelection selection = new ModelSelection();

    private String materialName;
    private String texturePath;
    private float[] materialColor;
    private Float materialShininess;
    private Float materialTransparency;
    private Float materialReflectivity;

    public Model() {
        this.vertices = new ArrayList<>();
        this.textureVertices = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.polygons = new ArrayList<>();
        this.materialColor = new float[]{1.0f, 1.0f, 1.0f};
    }

    public Model(List<Vector3f> vertices,
                 List<Vector2f> textureVertices,
                 List<Vector3f> normals,
                 List<Polygon> polygons,
                 String materialName,
                 String texturePath,
                 float[] materialColor,
                 Float materialShininess,
                 Float materialTransparency,
                 Float materialReflectivity) {
        this.vertices = new ArrayList<>(Objects.requireNonNull(vertices));
        this.textureVertices = new ArrayList<>(Objects.requireNonNull(textureVertices));
        this.normals = new ArrayList<>(Objects.requireNonNull(normals));
        this.polygons = new ArrayList<>(Objects.requireNonNull(polygons));
        this.materialName = materialName;
        this.texturePath = texturePath;
        this.materialColor = materialColor != null ? materialColor : new float[]{1.0f, 1.0f, 1.0f};
        this.materialShininess = materialShininess;
        this.materialTransparency = materialTransparency;
        this.materialReflectivity = materialReflectivity;
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

    public void invalidateTriangulation() {
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

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    public float[] getMaterialColor() {
        return materialColor;
    }

    public void setMaterialColor(float[] materialColor) {
        this.materialColor = materialColor != null ? materialColor : new float[]{1.0f, 1.0f, 1.0f};
    }

    public Float getMaterialShininess() {
        return materialShininess;
    }

    public void setMaterialShininess(Float materialShininess) {
        this.materialShininess = materialShininess;
    }

    public Float getMaterialTransparency() {
        return materialTransparency;
    }

    public void setMaterialTransparency(Float materialTransparency) {
        this.materialTransparency = materialTransparency;
    }

    public Float getMaterialReflectivity() {
        return materialReflectivity;
    }

    public void setMaterialReflectivity(Float materialReflectivity) {
        this.materialReflectivity = materialReflectivity;
    }

    public Model copy() {
        Model copy = new Model(
            vertices,
            textureVertices,
            normals,
            polygons,
            materialName,
            texturePath,
            materialColor != null ? materialColor.clone() : null,
            materialShininess,
            materialTransparency,
            materialReflectivity
        );
        copy.selection.clearAll();
        copy.useLighting = this.useLighting;
        copy.useTexture = this.useTexture;
        copy.drawPolygonalGrid = this.drawPolygonalGrid;
        return copy;
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

        Model copy = new Model(
            transformedVertices,
            new ArrayList<>(textureVertices),
            new ArrayList<>(normals),
            new ArrayList<>(polygons),
            materialName,
            texturePath,
            materialColor != null ? materialColor.clone() : null,
            materialShininess,
            materialTransparency,
            materialReflectivity
        );
        copy.selection.clearAll();
        copy.useLighting = this.useLighting;
        copy.useTexture = this.useTexture;
        copy.drawPolygonalGrid = this.drawPolygonalGrid;
        return copy;
    }

    public boolean isUseLighting() {
        return useLighting;
    }

    public void setUseLighting(boolean useLighting) {
        this.useLighting = useLighting;
    }

    public boolean isUseTexture() {
        return useTexture;
    }

    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    public boolean isDrawPolygonalGrid() {
        return drawPolygonalGrid;
    }

    public void setDrawPolygonalGrid(boolean drawPolygonalGrid) {
        this.drawPolygonalGrid = drawPolygonalGrid;
    }

    public ModelSelection getSelection() {
        return selection;
    }
}
