package ru.vsu.cs.cg.renderEngine;

import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.math.Vector4f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.rasterization.Rasterizer;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import ru.vsu.cs.cg.rasterization.Texture;
import ru.vsu.cs.cg.renderEngine.dto.RenderEntity;


public class RenderEngine {

    private static Model cameraGizmoModel;

    /**
     * Основной метод рендеринга
     * @param pixelWriter Объект для отрисовки пикселей на экран
     * @param width ширина экрана
     * @param height высота экрана
     * @param entities список моделей
     * @param cameras список камер
     * @param activeCamera активная камера
     * @param rasterizer экземпляр растеризатора
     * @param baseSettings основные настройки рендера
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

        for (RenderEntity entity : entities) {
            RasterizerSettings objectSettings = new RasterizerSettings(
                entity.isUseTexture(),
                entity.isUseLighting(),
                entity.isDrawPolygonalGrid(),
                entity.getColor() != null ? entity.getColor() : baseSettings.getDefaultColor(),
                baseSettings.getGridColor()
            );

            renderModel(
                pixelWriter, width, height,
                entity.getModel(),
                entity.getTranslation(), entity.getRotation(), entity.getScale(),
                viewProjectionMatrix, lightDirection,
                rasterizer, objectSettings, entity.getTexture()
            );
        }

        if (cameras != null) {
            Model gizmo = getCameraGizmo();
            RasterizerSettings gizmoSettings = new RasterizerSettings();
            gizmoSettings.setUseLighting(true);
            gizmoSettings.setDefaultColor(Color.ORANGE);

            for (Camera camera : cameras) {
                if (camera == activeCamera) continue;

                renderModel(
                    pixelWriter, width, height,
                    gizmo,
                    camera.getPosition(), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1),
                    viewProjectionMatrix, lightDirection,
                    rasterizer, gizmoSettings, null
                );
            }
        }
    }

    /**
     * Метод рендера модели
     * Здесь происходят все преобразования до растеризации
     * @param pixelWriter Объект для отрисовки пикселей на экран
     * @param width ширина экрана
     * @param height высота экрана
     * @param model модель для рендеринга
     * @param translation вектор позиции
     * @param rotation вектор поворота
     * @param scale вектор растяжения
     * @param viewProjectionMatrix матрица перспективы
     * @param lightDirection вектор света
     * @param rasterizer растеризатор
     * @param settings настройки модели
     * @param texture текстура модели
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

        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> normals = model.getNormals();
        List<Vector2f> textureVertices = model.getTextureVertices();
        List<Polygon> polygons = model.getTriangulatedPolygonsCache();

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> textureIndices = polygon.getTextureVertexIndices();
            List<Integer> normalIndices = polygon.getNormalIndices();

            Vector3f v1 = vertices.get(vertexIndices.get(0));
            Vector3f v2 = vertices.get(vertexIndices.get(1));
            Vector3f v3 = vertices.get(vertexIndices.get(2));

            Vector4f v1Clip = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvpMatrix, v1);
            Vector4f v2Clip = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvpMatrix, v2);
            Vector4f v3Clip = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvpMatrix, v3);

            if (v1Clip.getW() <= 0 || v2Clip.getW() <= 0 || v3Clip.getW() <= 0) continue;

            Vector3f v1NDC = v1Clip.toVector3Safe();
            Vector3f v2NDC = v2Clip.toVector3Safe();
            Vector3f v3NDC = v3Clip.toVector3Safe();

            if (v1NDC.getZ() > 1.0f && v2NDC.getZ() > 1.0f && v3NDC.getZ() > 1.0f) continue;
            if (v1NDC.getZ() < -1.0f && v2NDC.getZ() < -1.0f && v3NDC.getZ() < -1.0f) continue;


            Vector2f p1 = GraphicConveyor.vertexToPoint(v1NDC, width, height);
            Vector2f p2 = GraphicConveyor.vertexToPoint(v2NDC, width, height);
            Vector2f p3 = GraphicConveyor.vertexToPoint(v3NDC, width, height);

            Vector3f screenV1 = new Vector3f(p1.getX(), p1.getY(), v1NDC.getZ());
            Vector3f screenV2 = new Vector3f(p2.getX(), p2.getY(), v2NDC.getZ());
            Vector3f screenV3 = new Vector3f(p3.getX(), p3.getY(), v3NDC.getZ());

            Vector2f vt1 = (settings.isUseTexture() && textureIndices.size() > 0) ? textureVertices.get(textureIndices.get(0)) : null;
            Vector2f vt2 = (settings.isUseTexture() && textureIndices.size() > 1) ? textureVertices.get(textureIndices.get(1)) : null;
            Vector2f vt3 = (settings.isUseTexture() && textureIndices.size() > 2) ? textureVertices.get(textureIndices.get(2)) : null;

            Vector3f n1 = null, n2 = null, n3 = null;
            if (settings.isUseLighting() && !normalIndices.isEmpty()) {
                n1 = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, normals.get(normalIndices.get(0))).normalized();
                n2 = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, normals.get(normalIndices.get(1))).normalized();
                n3 = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, normals.get(normalIndices.get(2))).normalized();
            }

            rasterizer.drawTriangle(
                pixelWriter,
                width, height,
                screenV1, screenV2, screenV3,
                vt1, vt2, vt3,
                n1, n2, n3,
                texture, lightDirection, settings
            );
        }
    }


    private Model getCameraGizmo() {
        if (cameraGizmoModel == null) {
            cameraGizmoModel = new Model();
            float s = 0.2f;
            List<Vector3f> vs = new ArrayList<>();
            vs.add(new Vector3f(-s, -s, -s)); vs.add(new Vector3f(s, -s, -s));
            vs.add(new Vector3f(s, s, -s)); vs.add(new Vector3f(-s, s, -s));
            vs.add(new Vector3f(-s, -s, s)); vs.add(new Vector3f(s, -s, s));
            vs.add(new Vector3f(s, s, s)); vs.add(new Vector3f(-s, s, s));
            cameraGizmoModel.setVertices(vs);

            List<Vector3f> ns = new ArrayList<>();
            ns.add(new Vector3f(0, 1, 0));
            cameraGizmoModel.setNormals(ns);

            int[][] indices = {
                {0,1,2}, {0,2,3}, {4,5,6}, {4,6,7}, {0,4,7}, {0,7,3},
                {1,5,6}, {1,6,2}, {3,2,6}, {3,6,7}, {0,1,5}, {0,5,4}
            };
            for(int[] f : indices) {
                ArrayList<Integer> v = new ArrayList<>(); for(int i : f) v.add(i);
                cameraGizmoModel.addPolygon(new Polygon(v, new ArrayList<>(), new ArrayList<>()));
            }
        }
        return cameraGizmoModel;
    }
}
