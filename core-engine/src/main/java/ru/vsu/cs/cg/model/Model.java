package ru.vsu.cs.cg.model;


import ru.vsu.cs.cg.math.NormalCalculator;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;

import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Model {
    private List<Vector3f> vertices = new ArrayList<Vector3f>();
    private List<Vector2f> textureVertices = new ArrayList<Vector2f>();
    private List<Vector3f> normals = new ArrayList<Vector3f>();
    private List<Polygon> polygons = new ArrayList<Polygon>();

    private List<Polygon> triangulatedPolygonsCache = null;

    /**
     * Добавляет полигон в модель
     * говорит что нужно пересчитать триангуляцию
     * @param polygon добавляемый полигон
     */
    public void addPolygon(Polygon polygon){
        polygons.add(polygon);
        invalidateTriangulation();
    }

    /**
     * Возвращает полигоны после триангуляции
     * при необходимости пересчитывает их
     * @return полигоны после триангуляции
     */
    public List<Polygon> getTriangulatedPolygonsCache(){
        List<Polygon> cache = triangulatedPolygonsCache;

        if (cache == null){
            cache = computeTriangulation();
            triangulatedPolygonsCache = cache;
        }

        return cache;
    }

    /**
     * Делает триангуляцию для каждого полигона
     */
    private List<Polygon> computeTriangulation(){
        List<Polygon> triangulationPolygons = new ArrayList<>();

        for(Polygon polygon: polygons){
            triangulationPolygons.addAll(polygon.triangulate());
        }

        return Collections.unmodifiableList(triangulationPolygons);
    }

    public void recomputeNormals(){
        List<Vector3f> newNormals = NormalCalculator.computeVertexNormals(vertices, polygons);
        normals.clear();
        normals = newNormals;
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
}