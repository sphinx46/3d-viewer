package ru.vsu.cs.cg.renderEngine.camera;

import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;

public class CameraGizmo {
    private static Model cachedGizmo = null;

    /**
     * Создает или возвращает готовую модель (пирамидку), изображающую камеру.
     */
    public static Model getGizmoModel() {
        if (cachedGizmo != null) {
            return cachedGizmo;
        }

        Model model = new Model();

        float w = 0.4f; // полуширина
        float h = 0.4f; // полувысота
        float len = 0.8f; // длина


        model.addVertex(new Vector3f(-w, h, 0));  // 0
        model.addVertex(new Vector3f(w, h, 0));   // 1
        model.addVertex(new Vector3f(w, -h, 0));  // 2
        model.addVertex(new Vector3f(-w, -h, 0)); // 3
        // 4: Острие (линза)
        model.addVertex(new Vector3f(0, 0, len)); // 4

        // 2. Добавляем полигоны
        // В вашем классе Polygon нужны ArrayList<Integer>

        // Задняя стенка (2 треугольника)
        addTriangle(model, 0, 1, 2);
        addTriangle(model, 2, 3, 0);

        // Боковые грани
        addTriangle(model, 0, 1, 4); // Верх
        addTriangle(model, 1, 2, 4); // Право
        addTriangle(model, 2, 3, 4); // Низ
        addTriangle(model, 3, 0, 4); // Лево

        // Нормали можно пересчитать автоматически вашим методом
        model.recomputeNormals();

        cachedGizmo = model;
        return model;
    }

    private static void addTriangle(Model model, int v1, int v2, int v3) {
        Polygon p = new Polygon();
        ArrayList<Integer> verts = new ArrayList<>();
        verts.add(v1);
        verts.add(v2);
        verts.add(v3);

        p.setVertexIndices(verts);

        // Пустые списки для текстур и нормалей, чтобы не было NullPointer
        p.setTextureVertexIndices(new ArrayList<>());
        p.setNormalIndices(new ArrayList<>());

        model.addPolygon(p);
    }
}