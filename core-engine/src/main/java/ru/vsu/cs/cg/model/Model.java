package ru.vsu.cs.cg.model;


import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;

import java.util.ArrayList;

public final class Model {
    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    private ArrayList<Polygon> triangulatedPolygon = null;
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
    public ArrayList<Polygon> getTriangulatedPolygon(){
        if (needsTriangulation || triangulatedPolygon == null){
            computeTriangulation();
        }
        return triangulatedPolygon;
    }

    /**
     * Делает триангуляцию для каждого полигона
     */
    private void computeTriangulation(){
        triangulatedPolygon = new ArrayList<>();

        for(Polygon polygon: polygons){
            triangulatedPolygon.addAll(polygon.triangulate());
        }

        needsTriangulation = false;
    }

    /**
     * Если требуется произвести триангуляцию
     */
    public void markForTriangulation() {
        needsTriangulation = true;
    }
}