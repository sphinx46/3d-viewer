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

    public void render(
        PixelWriter pixelWriter,
        int width,
        int height,
        List<RenderEntity> entities,
        List<Camera> cameras,
        Camera activeCamera,
        Rasterizer rasterizer,
        RasterizerSettings baseSettings,
        boolean gridVisible) {

        if (activeCamera == null) return;

        Matrix4x4 viewMatrix = activeCamera.getViewMatrix();
        Matrix4x4 projectionMatrix = activeCamera.getProjectionMatrix();
        Matrix4x4 viewProjectionMatrix = projectionMatrix.multiply(viewMatrix);

        if (gridVisible) {
            renderGrid(pixelWriter, width, height, activeCamera, rasterizer, baseSettings);
        }

        Vector3f lightDirection = activeCamera.getLightDirection();

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
                renderObjectGizmo(pixelWriter, width, height, entity, activeCamera, rasterizer);
            }
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

            Vector3f screenV1 = new Vector3f(p1.getX(), p1.getY(), v1Clip.getW());
            Vector3f screenV2 = new Vector3f(p2.getX(), p2.getY(), v2Clip.getW());
            Vector3f screenV3 = new Vector3f(p3.getX(), p3.getY(), v3Clip.getW());

            Vector2f vt1 = (settings.isUseTexture() && textureIndices.size() > 0) ? textureVertices.get(textureIndices.get(0)) : null;
            Vector2f vt2 = (settings.isUseTexture() && textureIndices.size() > 1) ? textureVertices.get(textureIndices.get(1)) : null;
            Vector2f vt3 = (settings.isUseTexture() && textureIndices.size() > 2) ? textureVertices.get(textureIndices.get(2)) : null;

            Vector3f n1 = null, n2 = null, n3 = null;
            if (settings.isUseLighting() && !normalIndices.isEmpty()) {
                n1 = GraphicConveyor.multiplyMatrix4ByVector3Normal(modelMatrix, normals.get(normalIndices.get(0))).normalized();
                n2 = GraphicConveyor.multiplyMatrix4ByVector3Normal(modelMatrix, normals.get(normalIndices.get(1))).normalized();
                n3 = GraphicConveyor.multiplyMatrix4ByVector3Normal(modelMatrix, normals.get(normalIndices.get(2))).normalized();
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

    private void renderGrid(
        PixelWriter pixelWriter,
        int width,
        int height,
        Camera activeCamera,
        Rasterizer rasterizer,
        RasterizerSettings baseSettings) {

        int size = 12;
        Color gridColor = Color.GRAY;
        Color mainAxisColor = Color.WHITE;

        Matrix4x4 viewMatrix = activeCamera.getViewMatrix();
        Matrix4x4 projectionMatrix = activeCamera.getProjectionMatrix();
        Matrix4x4 viewProjectionMatrix = projectionMatrix.multiply(viewMatrix);

        for (int i = -size; i <= size; i++) {
            Color color = (i == 0) ? mainAxisColor : gridColor;

            Vector3f p1 = new Vector3f(i, 0, -size);
            Vector3f p2 = new Vector3f(i, 0, size);

            processAndDrawLine(pixelWriter, width, height, p1, p2, viewProjectionMatrix, rasterizer, color);

            Vector3f p3 = new Vector3f(-size, 0, i);
            Vector3f p4 = new Vector3f(size, 0, i);

            processAndDrawLine(pixelWriter, width, height, p3, p4, viewProjectionMatrix, rasterizer, color);
        }
    }

    private void processAndDrawLine(
        PixelWriter pixelWriter,
        int width,
        int height,
        Vector3f p1,
        Vector3f p2,
        Matrix4x4 vpMatrix,
        Rasterizer rasterizer,
        Color color) {

        Vector4f v1 = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(vpMatrix, p1);
        Vector4f v2 = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(vpMatrix, p2);

        float near = 0.1f;
        if (v1.getW() < near && v2.getW() < near) {
            return;
        }

        if (v1.getW() < near) {
            float t = (near - v1.getW()) / (v2.getW() - v1.getW());
            v1 = lerpVector4(v1, v2, t);
        } else if (v2.getW() < near) {
            float t = (near - v2.getW()) / (v1.getW() - v2.getW());
            v2 = lerpVector4(v2, v1, t);
        }

        Vector3f ndc1 = v1.toVector3Safe();
        Vector3f ndc2 = v2.toVector3Safe();

        Vector2f screen1 = GraphicConveyor.vertexToPoint(ndc1, width, height);
        Vector2f screen2 = GraphicConveyor.vertexToPoint(ndc2, width, height);

        Vector3f s1 = new Vector3f(screen1.getX(), screen1.getY(), v1.getW());
        Vector3f s2 = new Vector3f(screen2.getX(), screen2.getY(), v2.getW());

        rasterizer.drawLine(pixelWriter, width, height, s1, s2, color);
    }

    private Vector4f lerpVector4(Vector4f a, Vector4f b, float t) {
        return new Vector4f(
            a.getX() + (b.getX() - a.getX()) * t,
            a.getY() + (b.getY() - a.getY()) * t,
            a.getZ() + (b.getZ() - a.getZ()) * t,
            a.getW() + (b.getW() - a.getW()) * t
        );
    }

    public void renderObjectGizmo(
        PixelWriter pixelWriter,
        int width,
        int height,
        RenderEntity entity,
        Camera activeCamera,
        Rasterizer rasterizer) {

        if (entity == null || activeCamera == null) return;

        Matrix4x4 viewMatrix = activeCamera.getViewMatrix();
        Matrix4x4 projectionMatrix = activeCamera.getProjectionMatrix();
        Matrix4x4 viewProjectionMatrix = projectionMatrix.multiply(viewMatrix);

        Matrix4x4 modelMatrix = GraphicConveyor.rotateScaleTranslate(
            entity.getTranslation(),
            entity.getRotation(),
            entity.getScale()
        );

        Matrix4x4 mvpMatrix = viewProjectionMatrix.multiply(modelMatrix);

        float axisLength = 1.5f;

        Vector3f center = new Vector3f(0, 0, 0);

        Vector3f xAxis = new Vector3f(axisLength, 0, 0);
        Vector3f yAxis = new Vector3f(0, axisLength, 0);
        Vector3f zAxis = new Vector3f(0, 0, axisLength);

        renderLineWithMatrix(pixelWriter, width, height, center, xAxis, Color.RED, mvpMatrix, rasterizer);
        renderLineWithMatrix(pixelWriter, width, height, center, yAxis, Color.GREEN, mvpMatrix, rasterizer);
        renderLineWithMatrix(pixelWriter, width, height, center, zAxis, Color.BLUE, mvpMatrix, rasterizer);
    }

    private void renderLineWithMatrix(
        PixelWriter pixelWriter,
        int width,
        int height,
        Vector3f p1, Vector3f p2,
        Color color,
        Matrix4x4 mvpMatrix,
        Rasterizer rasterizer) {

        Vector4f v1 = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvpMatrix, p1);
        Vector4f v2 = GraphicConveyor.multiplyMatrix4ByVector3ToVector4(mvpMatrix, p2);

        float nearPlaneW = 0.1f;
        if (v1.getW() < nearPlaneW && v2.getW() < nearPlaneW) return;
        if (v1.getW() < nearPlaneW || v2.getW() < nearPlaneW) {
            float t = (nearPlaneW - v1.getW()) / (v2.getW() - v1.getW());
            Vector4f clippedPoint = new Vector4f(
                v1.getX() + (v2.getX() - v1.getX()) * t,
                v1.getY() + (v2.getY() - v1.getY()) * t,
                v1.getZ() + (v2.getZ() - v1.getZ()) * t,
                nearPlaneW
            );
            if (v1.getW() < nearPlaneW) v1 = clippedPoint;
            else v2 = clippedPoint;
        }

        Vector3f v1NDC = v1.toVector3Safe();
        Vector3f v2NDC = v2.toVector3Safe();

        Vector2f p1Screen = GraphicConveyor.vertexToPoint(v1NDC, width, height);
        Vector2f p2Screen = GraphicConveyor.vertexToPoint(v2NDC, width, height);

        Vector3f screenV1 = new Vector3f(p1Screen.getX(), p1Screen.getY(), v1.getW());
        Vector3f screenV2 = new Vector3f(p2Screen.getX(), p2Screen.getY(), v2.getW());

        rasterizer.drawLine(pixelWriter, width, height, screenV1, screenV2, color, true);
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
