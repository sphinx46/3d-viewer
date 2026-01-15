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
     */
    public static List<Vector3f> computeVertexNormals(List<Vector3f> vertices, List<Polygon> polygons) {

        List<Vector3f> normals = new ArrayList<>();

        Vector3f[] sumNormalsArray = new Vector3f[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            sumNormalsArray[i] = new Vector3f();
        }

        for (Polygon polygon : polygons) {
            List<Integer> indices = polygon.getVertexIndices();

            if (indices.size() < 3) continue;

            Vector3f v0 = vertices.get(indices.get(0));
            Vector3f v1 = vertices.get(indices.get(1));
            Vector3f v2 = vertices.get(indices.get(2));

            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);

            Vector3f polygonNormal = edge1.cross(edge2);

            for (int idx : indices) {
                sumNormalsArray[idx] = sumNormalsArray[idx].add(polygonNormal);
            }
        }

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f normal = sumNormalsArray[i];

            if (normal.length() == 0) {
                normals.add(new Vector3f(0, 1, 0));
            } else {
                normals.add(normal.normalized());
            }
        }
        return normals;
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

