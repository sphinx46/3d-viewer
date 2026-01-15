package ru.vsu.cs.cg.renderEngine;

import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.math.Vector4f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.model.selection.ModelSelection;
import ru.vsu.cs.cg.rasterization.Rasterizer;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javafx.scene.paint.Color;
import ru.vsu.cs.cg.rasterization.Texture;
import ru.vsu.cs.cg.renderEngine.dto.RenderEntity;

/**
 * Основной класс для рендеринга 3D сцены.
 * Обрабатывает камеры, объекты, освещение и отрисовывает их на экран.
 */
public class RenderEngine {

    private static Model cameraGizmoModel;

    /**
     * Главный метод рендеринга сцены.
     *
     * @param pixelWriter     Писатель пикселей в буфер
     * @param width           Ширина области рендеринга
     * @param height          Высота области рендеринга
     * @param entities        Список объектов для рендеринга
     * @param cameras         Список камер в сцене
     * @param activeCamera    Активная камера (через которую видим сцену)
     * @param rasterizer      Растеризатор для отрисовки треугольников
     * @param baseSettings    Базовые настройки рендеринга
     */
    public void render(
            PixelWriter pixelWriter,
            int width,
            int height,
            List<RenderEntity> entities,
            List<Camera> cameras,
            Camera activeCamera,
            Rasterizer rasterizer,
            RasterizerSettings baseSettings) {

        if (activeCamera == null) return;

        Matrix4x4 viewMatrix = activeCamera.getViewMatrix();
        Matrix4x4 projectionMatrix = activeCamera.getProjectionMatrix();
        Matrix4x4 viewProjectionMatrix = projectionMatrix.multiply(viewMatrix);


        Vector3f lightDirection = activeCamera.getLightDirection();

        renderGrid(pixelWriter, width, height, activeCamera, viewProjectionMatrix, rasterizer, baseSettings);

        for (RenderEntity entity : entities) {
            RasterizerSettings objectSettings = entity.getSettings().copy();

            renderModel(
                    pixelWriter, width, height,
                    entity.getModel(),
                    entity.getTranslation(), entity.getRotation(), entity.getScale(),
                    viewProjectionMatrix, lightDirection,
                    rasterizer, objectSettings, entity.getTexture()
            );

            if (objectSettings.isDrawAxisLines()){
                renderObjectGizmo(
                        pixelWriter, width, height,
                        entity,
                        viewProjectionMatrix,
                        rasterizer);
            }
        }

        if (cameras != null) {
            renderCameraGizmos(
                    pixelWriter, width, height,
                    cameras, activeCamera,
                    viewProjectionMatrix,
                    lightDirection,
                    rasterizer);
        }

    }

    /**
     * Рендерит одну 3D модель.
     *
     * @param pixelWriter          Писатель пикселей
     * @param width                Ширина области
     * @param height               Высота области
     * @param model                3D модель для отрисовки
     * @param translation          Позиция модели в мире
     * @param rotation             Вращение модели
     * @param scale                Масштаб модели
     * @param viewProjectionMatrix Комбинированная матрица вида и проекции
     * @param lightDirection       Направление источника света
     * @param rasterizer           Растеризатор
     * @param settings             Настройки рендеринга для этой модели
     * @param texture              Текстура модели (может быть null)
     */
    private void renderModel(
            PixelWriter pixelWriter,
            int width,
            int height,
            Model model,
            Vector3f translation, Vector3f rotation, Vector3f scale,
            Matrix4x4 viewProjectionMatrix,
            Vector3f lightDirection,
            Rasterizer rasterizer,
            RasterizerSettings settings,
            Texture texture) {

        Matrix4x4 modelMatrix = GraphicConveyor.rotateScaleTranslate(translation, rotation, scale);
        Matrix4x4 mvpMatrix = viewProjectionMatrix.multiply(modelMatrix);

        Matrix4x4 normalMatrix = modelMatrix.inverse().transpose();

        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> normals = model.getNormals();
        List<Vector2f> textureVertices = model.getTextureVertices();
        List<Polygon> polygons = model.getTriangulatedPolygonsCache();

        for (Polygon polygon : polygons) {
            List<Integer> vIdx = polygon.getVertexIndices();
            List<Integer> tIdx = polygon.getTextureVertexIndices();
            List<Integer> nIdx = polygon.getNormalIndices();

            Vector4f v1Clip = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvpMatrix, vertices.get(vIdx.get(0)));
            Vector4f v2Clip = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvpMatrix, vertices.get(vIdx.get(1)));
            Vector4f v3Clip = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvpMatrix, vertices.get(vIdx.get(2)));

            if (v1Clip.getW() < 0.1f || v2Clip.getW() < 0.1f || v3Clip.getW() < 0.1f) continue;

            Vector3f screenV1 = toScreenSpace(v1Clip, width, height);
            Vector3f screenV2 = toScreenSpace(v2Clip, width, height);
            Vector3f screenV3 = toScreenSpace(v3Clip, width, height);

            Vector2f vt1 = (settings.isUseTexture() && tIdx.size() > 0) ? textureVertices.get(tIdx.get(0)) : null;
            Vector2f vt2 = (settings.isUseTexture() && tIdx.size() > 1) ? textureVertices.get(tIdx.get(1)) : null;
            Vector2f vt3 = (settings.isUseTexture() && tIdx.size() > 2) ? textureVertices.get(tIdx.get(2)) : null;

            Vector3f n1 = null, n2 = null, n3 = null;
            if (settings.isUseLighting() && !nIdx.isEmpty()) {
                n1 = GraphicConveyor.multiplyMatrix4ByVector3Normal(normalMatrix, normals.get(nIdx.get(0))).normalizeSafe();
                n2 = GraphicConveyor.multiplyMatrix4ByVector3Normal(normalMatrix, normals.get(nIdx.get(1))).normalizeSafe();
                n3 = GraphicConveyor.multiplyMatrix4ByVector3Normal(normalMatrix, normals.get(nIdx.get(2))).normalizeSafe();
            }

            rasterizer.drawTriangle(pixelWriter, width, height,
                    screenV1, screenV2, screenV3,
                    vt1, vt2, vt3,
                    n1, n2, n3,
                    texture, lightDirection,
                    settings);
        }

        renderSelection(pixelWriter, width, height,
                model, mvpMatrix,
                rasterizer);
    }

    /**
     * Отрисовывает 3D линию с отсечением по ближней плоскости.
     *
     * @param pw        Писатель пикселей
     * @param w         Ширина области рендеринга
     * @param h         Высота области рендеринга
     * @param p1        Начальная точка линии в мировых координатах
     * @param p2        Конечная точка линии в мировых координатах
     * @param mvp       Матрица Model-View-Projection
     * @param color     Цвет линии
     * @param r         Растеризатор
     * @param useZBuffer Флаг использования буфера глубины
     */
    private void renderLine3D(PixelWriter pw, int w, int h, Vector3f p1, Vector3f p2, Matrix4x4 mvp, Color color, Rasterizer r, boolean useZBuffer) {
        Vector4f v1 = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvp, p1);
        Vector4f v2 = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvp, p2);

        float near = 0.1f;
        if (v1.getW() < near && v2.getW() < near) return;

        if (v1.getW() < near || v2.getW() < near) {
            float t = (near - v1.getW()) / (v2.getW() - v1.getW());
            Vector4f clipped = lerpVector4(v1, v2, t);
            if (v1.getW() < near) v1 = clipped; else v2 = clipped;
        }

        Vector3f s1 = toScreenSpace(v1, w, h);
        Vector3f s2 = toScreenSpace(v2, w, h);
        r.drawLine(pw, w, h, s1, s2, color, useZBuffer);
    }

    /**
     * Рендерит координатную сетку на плоскости XZ.
     *
     * @param pw Писатель пикселей
     * @param w  Ширина области рендеринга
     * @param h  Высота области рендеринга
     * @param cam Камера (для позиционирования сетки)
     * @param vp  Матрица View-Projection
     * @param r   Растеризатор
     * @param s   Настройки рендеринга
     */
    private void renderGrid(PixelWriter pw, int w, int h, Camera cam, Matrix4x4 vp, Rasterizer r, RasterizerSettings s) {
        if (!s.isDrawGrid()) return;

        int radius = 20;
        int camX = (int) Math.round(cam.getPosition().getX());
        int camZ = (int) Math.round(cam.getPosition().getZ());

        for (int i = -radius; i <= radius; i++) {
            int x = camX + i;
            int z = camZ + i;

            Color colorX = (x == 0) ? Color.WHITE : Color.GRAY;
            Color colorZ = (z == 0) ? Color.WHITE : Color.GRAY;

            renderLine3D(pw, w, h,
                    new Vector3f(x, 0, camZ - radius),
                    new Vector3f(x, 0, camZ + radius),
                    vp, colorX, r, false);
            renderLine3D(pw, w, h,
                    new Vector3f(camX - radius, 0, z),
                    new Vector3f(camX + radius, 0, z),
                    vp, colorZ, r, false);
        }
    }

    /**
     * Рендерит выделение вершин и полигонов модели.
     *
     * @param pw    Писатель пикселей
     * @param w     Ширина области рендеринга
     * @param h     Высота области рендеринга
     * @param model Модель с выделенными элементами
     * @param mvp   Матрица Model-View-Projection
     * @param r     Растеризатор
     */
    /**
     * Рендерит выделение вершин и треугольников модели.
     *
     * @param pw    Писатель пикселей
     * @param w     Ширина области рендеринга
     * @param h     Высота области рендеринга
     * @param model Модель с выделенными элементами
     * @param mvp   Матрица Model-View-Projection
     * @param r     Растеризатор
     */
    /**
     * Рендерит выделение вершин и треугольников модели.
     *
     * @param pw    Писатель пикселей
     * @param w     Ширина области рендеринга
     * @param h     Высота области рендеринга
     * @param model Модель с выделенными элементами
     * @param mvp   Матрица Model-View-Projection
     * @param r     Растеризатор
     */
    private void renderSelection(PixelWriter pw, int w, int h, Model model, Matrix4x4 mvp, Rasterizer r) {
        ModelSelection sel = model.getSelection();

        if (sel.hasSelectedVertices()) {
            for (Integer vertexIdx : sel.getSelectedVertices()) {
                if (vertexIdx >= 0 && vertexIdx < model.getVertices().size()) {
                    Vector4f v = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(
                        mvp, model.getVertices().get(vertexIdx));
                    if (v.getW() > 0.1f) {
                        renderVertexPoint(pw, w, h,
                            GraphicConveyor.vertexToPoint(v.toVector3Safe(), w, h),
                            v.getW(), 8, Color.YELLOW, r);
                    }
                }
            }
        }

        if (sel.hasSelectedTriangles()) {
            List<Polygon> triangles = model.getTriangulatedPolygonsCache();
            Set<Integer> selectedTriangleIndices = sel.getSelectedTriangles();

            for (Integer triangleIdx : selectedTriangleIndices) {
                if (triangleIdx >= 0 && triangleIdx < triangles.size()) {
                    Polygon triangle = triangles.get(triangleIdx);
                    List<Integer> vIdx = triangle.getVertexIndices();

                    for (int i = 0; i < 3; i++) {
                        Vector3f v1 = model.getVertices().get(vIdx.get(i));
                        Vector3f v2 = model.getVertices().get(vIdx.get((i + 1) % 3));
                        renderLine3D(pw, w, h, v1, v2, mvp, Color.CYAN, r, true);
                    }
                }
            }
        }
    }

    /**
     * Линейная интерполяция между двумя векторами 4D.
     */
    private Vector4f lerpVector4(Vector4f a, Vector4f b, float t) {
        return new Vector4f(
                a.getX() + (b.getX() - a.getX()) * t,
                a.getY() + (b.getY() - a.getY()) * t,
                a.getZ() + (b.getZ() - a.getZ()) * t,
                a.getW() + (b.getW() - a.getW()) * t);
    }

    /**
     * Преобразует координаты отсечения в экранные координаты.
     */
    private Vector3f toScreenSpace(Vector4f vClip, int w, int h) {
        Vector3f ndc = vClip.toVector3Safe();
        Vector2f p = GraphicConveyor.vertexToPoint(ndc, w, h);
        return new Vector3f(p.getX(), p.getY(), vClip.getW());
    }

    /**
     * Рендерит оси объекта (гизмо) - X, Y, Z.
     */
    public void renderObjectGizmo(PixelWriter pw, int w, int h, RenderEntity entity, Matrix4x4 vp, Rasterizer r) {
        Matrix4x4 modelMatrix = GraphicConveyor.rotateScaleTranslate(entity.getTranslation(), entity.getRotation(), entity.getScale());
        Matrix4x4 mvp = vp.multiply(modelMatrix);

        float len = 1.5f;
        renderLine3D(pw, w, h, new Vector3f(0,0,0), new Vector3f(len,0,0), mvp, Color.RED, r, true);
        renderLine3D(pw, w, h, new Vector3f(0,0,0), new Vector3f(0,len,0), mvp, Color.GREEN, r, true);
        renderLine3D(pw, w, h, new Vector3f(0,0,0), new Vector3f(0,0,len), mvp, Color.BLUE, r, true);
    }

    /**
     * Рендерит значки камер в сцене.
     */
    private void renderCameraGizmos(PixelWriter pw, int w, int h, List<Camera> cameras, Camera active, Matrix4x4 vp, Vector3f light, Rasterizer r) {
        Model gizmo = getCameraGizmo();
        RasterizerSettings s = new RasterizerSettings();
        s.setUseLighting(false);
        s.setDefaultColor(Color.GRAY);

        for (Camera c : cameras) {
            if (c == active) continue;
            renderModel(pw, w, h,
                    gizmo, c.getPosition(),
                    new Vector3f(0,0,0), new Vector3f(1,1,1),
                    vp, light,
                    r, s,
                    null);
        }
    }

    /**
     * Рендерит точку вершины как квадрат.
     */
    private void renderVertexPoint(PixelWriter pw, int w, int h, Vector2f center, float depth, float size, Color color, Rasterizer r) {
        float hs = size / 2.0f;
        int x1 = Math.max(0, (int)(center.getX() - hs));
        int x2 = Math.min(w - 1, (int)(center.getX() + hs));
        int y1 = Math.max(0, (int)(center.getY() - hs));
        int y2 = Math.min(h - 1, (int)(center.getY() + hs));

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                r.drawPixel(pw, x, y, depth, color);
            }
        }
    }

    /**
     * Возвращает модель значка камеры (куб).
     * Создается при первом вызове.
     */
    private Model getCameraGizmo() {
        if (cameraGizmoModel == null) {
            cameraGizmoModel = new Model();

            float s = 0.5f;

            List<Vector3f> vs = new ArrayList<>();

            vs.add(new Vector3f(-s, -s, -s));
            vs.add(new Vector3f(s, -s, -s));
            vs.add(new Vector3f(s, s, -s));
            vs.add(new Vector3f(-s, s, -s));
            vs.add(new Vector3f(-s, -s, s));
            vs.add(new Vector3f(s, -s, s));
            vs.add(new Vector3f(s, s, s));
            vs.add(new Vector3f(-s, s, s));
            cameraGizmoModel.setVertices(vs);


            List<Vector3f> ns = new ArrayList<>();
            ns.add(new Vector3f(0, 1, 0));
            cameraGizmoModel.setNormals(ns);

            int[][] indices = {
                    {0, 1, 2}, {0, 2, 3},
                    {5, 4, 7}, {5, 7, 6},
                    {3, 2, 6}, {3, 6, 7},
                    {1, 0, 4}, {1, 4, 5},
                    {4, 0, 3}, {4, 3, 7},
                    {1, 5, 6}, {1, 6, 2}
            };

            for (int[] face : indices) {
                ArrayList<Integer> v = new ArrayList<>();
                ArrayList<Integer> n = new ArrayList<>();
                ArrayList<Integer> t = new ArrayList<>();

                for (int i : face) {
                    v.add(i);
                    n.add(0);
                }
                cameraGizmoModel.addPolygon(new Polygon(v, t, n));
            }
        }
        return cameraGizmoModel;
    }
}
