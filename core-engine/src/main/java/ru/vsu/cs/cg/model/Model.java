package ru.vsu.cs.cg.model;


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

    private volatile List<Polygon> triangulatedPolygonsCache = null;
    private boolean needsTriangulation = true;

    /**
     * Добавляет полигон в модель
     * говорит что нужно пересчитать триангуляцию
     * @param polygon добавляемый полигон
     */
    public void addPolygon(Polygon polygon){
        polygons.add(polygon);
        needsTriangulation = true;
    }

    /**
     * Возвращает полигоны после триангуляции
     * при необходимости пересчитывает их
     * @return полигоны после триангуляции
     */
    public List<Polygon> getTriangulatedPolygonsCache(){
        List<Polygon> cache = triangulatedPolygonsCache;

        if (cache == null || needsTriangulation){
            synchronized (this){
                cache = triangulatedPolygonsCache;
                if (cache == null || needsTriangulation){
                    cache = computeTriangulation();
                    triangulatedPolygonsCache = cache;
                    needsTriangulation = false;
                }
            }
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

    /**
     * Если требуется произвести триангуляцию
     */
    public void markForTriangulation() {
        needsTriangulation = true;
    }

    public List<Vector3f> getVertices() {
        return vertices;
    }

    public List<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public List<Vector3f> getNormals() {
        return normals;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public void setVertices(List<Vector3f> vertices) {
        this.vertices = vertices;
    }

    public void setTextureVertices(List<Vector2f> textureVertices) {
        this.textureVertices = textureVertices;
    }

    public void setNormals(List<Vector3f> normals) {
        this.normals = normals;
    }

    public void setPolygons(List<Polygon> polygons) {
        this.polygons = polygons;
    }
}