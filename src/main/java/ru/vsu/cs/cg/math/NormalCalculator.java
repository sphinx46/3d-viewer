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

        Vector3f[] sumNormalsArray = new Vector3f[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            sumNormals.put(i, new Vector3f());
            sumNormalsArray[i] = new Vector3f();
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

            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);

            Vector3f polygonNormal = edge1.cross(edge2);

            for (int idx : indices) {
                Vector3f normal = sumNormals.get(idx).add(polygonNormal);
                sumNormalsArray[idx] = normal;
                sumNormals.put(idx, normal);
            }
        }

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f normal = sumNormalsArray[i];

            if (normal.length() == 0) {
                normals.add(new Vector3f(0, 0, 0));
            } else {
                normals.add(normal.normalized());
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

        Vector3f edge1 = v1.subtract(v0);
        Vector3f edge2 = v2.subtract(v0);

        Vector3f polygonNormal = edge1.cross(edge2);

        return polygonNormal.normalized();
    }
}

