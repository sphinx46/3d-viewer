package ru.vsu.cs.cg.math;


import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalCalculator {

    /**
     * Высчитывает нормали вершин объекта
     * (при необходимости пересчитывает)
     *
     * @param model объект
     */
    public static void calculateVerticesNormals(Model model) {
        ArrayList<Vector3f> vertices =  model.vertices;
        model.normals.clear();

        ArrayList<Vector3f> normals = model.normals;
        Map<Integer, Vector3f> sumNormals = new HashMap<>();

        for (int i = 0; i < vertices.size(); i++) {
            sumNormals.put(i, new Vector3f());
        }

        for (Polygon polygon : model.polygons) {
            List<Integer> indices = polygon.getVertexIndices();

            List<Integer> normalIndices = polygon.getNormalIndices();
            normalIndices.clear();
            normalIndices.addAll(indices);

            if (indices.size() < 3) continue;

            Vector3f v0 = vertices.get(indices.get(0));
            Vector3f v1 = vertices.get(indices.get(1));
            Vector3f v2 = vertices.get(indices.get(2));

            Vector3f edge1 = Vector3f.subtract(v1, v0);
            Vector3f edge2 = Vector3f.subtract(v2, v0);

            Vector3f polygonNormal = Vector3f.cross(edge1, edge2);

            for (int idx : indices) {
                sumNormals.get(idx).sum(polygonNormal);
            }
        }

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f normal = sumNormals.get(i);

            if (normal.length() == 0) {
                normals.add(new Vector3f(0, 0, 0));
            } else {
                normal.normalize();
                normals.add(normal);
            }
        }
    }

    /**
     * высчитывает нормаль полигона
     * (сделано на всякий случай)
     *
     * @param v0 1 вектор
     * @param v1 2 вектор
     * @param v2 3 вектор
     * @return посчитанную нормаль полигона
     */

    public static Vector3f calculatePolygonNormal(
            Vector3f v0, Vector3f v1, Vector3f v2) {

        Vector3f edge1 = Vector3f.subtract(v1, v0);
        Vector3f edge2 = Vector3f.subtract(v2, v0);

        Vector3f normal = Vector3f.cross(edge1, edge2);
        normal.normalize();

        return normal;
    }
}

