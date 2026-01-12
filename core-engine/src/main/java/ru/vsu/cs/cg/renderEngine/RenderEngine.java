package ru.vsu.cs.cg.renderEngine;

import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.rasterization.Rasterizer;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.renderEngine.camera.Camera;
import ru.vsu.cs.cg.renderEngine.camera.CameraGizmo;

import java.util.List;

public class RenderEngine {
    private final Rasterizer rasterizer;
    private final RasterizerSettings settings;
    private final int width;
    private final int height;

    public RenderEngine(Rasterizer rasterizer, RasterizerSettings settings, int width, int height) {
        this.rasterizer = rasterizer;
        this.settings = settings;
        this.width = width;
        this.height = height;
    }

    public void drawScene(SceneManager sceneManager, PixelWriter pixelWriter) {
        Camera activeCam = sceneManager.getActiveCamera();

        Matrix4x4 viewMatrix = activeCam.getViewMatrix();
        Matrix4x4 projectionMatrix = activeCam.getProjectionMatrix();
        Matrix4x4 viewProjection = projectionMatrix.multiply(viewMatrix);

        for (Model model : sceneManager.getModels()) { // тут как будто лучше использовать твою версию модели,
                                                       // тк она хранит текстуру, но опять же нет доступа к ней
            Matrix4x4 modelMatrix = GraphicConveyor.rotateScaleTranslate(
                    model.getTranslation(),
                    model.getRotation(),
                    model.getScale()
            );

            Matrix4x4 mvpMatrix = viewProjection.multiply(modelMatrix);

            // Рисуем модель
            drawModel(model, mvpMatrix, pixelWriter, settings);
        }

        Model gizmoModel = CameraGizmo.getGizmoModel();

        for (Camera cam : sceneManager.getCameras()) {
            if (cam == activeCam) continue;

            Matrix4x4 cameraWorldMatrix = createCameraWorldMatrix(cam);
            Matrix4x4 mvpMatrix = viewProjection.multiply(cameraWorldMatrix);

            // Рисуем гизмо, но с уникальной матрицей
            drawModel(gizmoModel, mvpMatrix, pixelWriter, settings);
        }
    }

    /**
     * Универсальный метод отрисовки любой модели с заданной MVP матрицей
     */
    private void drawModel(Model model, Matrix4x4 mvpMatrix, PixelWriter pixelWriter, RasterizerSettings settings) {
        List<Vector3f> vertices = model.getVertices();
        List<Vector2f> textures = model.getTextureVertices();
        List<Vector3f> normals = model.getNormals();

        // Используем кэш триангулированных полигонов из вашего класса Model!
        List<Polygon> polygons = model.getTriangulatedPolygonsCache();

        for (Polygon polygon : polygons) {
            List<Integer> vIndices = polygon.getVertexIndices();
            List<Integer> tIndices = polygon.getTextureVertexIndices();
            List<Integer> nIndices = polygon.getNormalIndices();

            // Проверка на корректность треугольника
            if (vIndices.size() < 3) continue;

            // Извлекаем данные вершин по индексам
            Vector3f v1 = vertices.get(vIndices.get(0));
            Vector3f v2 = vertices.get(vIndices.get(1));
            Vector3f v3 = vertices.get(vIndices.get(2));

            // Извлекаем UV (если есть)
            Vector2f uv1 = (!tIndices.isEmpty() && tIndices.size() >= 3) ? textures.get(tIndices.get(0)) : new Vector2f(0,0);
            Vector2f uv2 = (!tIndices.isEmpty() && tIndices.size() >= 3) ? textures.get(tIndices.get(1)) : new Vector2f(0,1);
            Vector2f uv3 = (!tIndices.isEmpty() && tIndices.size() >= 3) ? textures.get(tIndices.get(2)) : new Vector2f(1,1);

            // Извлекаем нормали (если есть)
            Vector3f n1 = (!nIndices.isEmpty() && nIndices.size() >= 3) ? normals.get(nIndices.get(0)) : new Vector3f(0,1,0);
            Vector3f n2 = (!nIndices.isEmpty() && nIndices.size() >= 3) ? normals.get(nIndices.get(1)) : new Vector3f(0,1,0);
            Vector3f n3 = (!nIndices.isEmpty() && nIndices.size() >= 3) ? normals.get(nIndices.get(2)) : new Vector3f(0,1,0);

            // Трансформация вершин
            Vector3f v1Clip = GraphicConveyor.multiplyMatrix4ByVector3(mvpMatrix, v1);
            Vector3f v2Clip = GraphicConveyor.multiplyMatrix4ByVector3(mvpMatrix, v2);
            Vector3f v3Clip = GraphicConveyor.multiplyMatrix4ByVector3(mvpMatrix, v3);

            // Перевод в экранные координаты
            Vector2f p1 = GraphicConveyor.vertexToPoint(v1Clip, width, height);
            Vector2f p2 = GraphicConveyor.vertexToPoint(v2Clip, width, height);
            Vector2f p3 = GraphicConveyor.vertexToPoint(v3Clip, width, height);

            // Собираем вершины с Z-координатой для Z-буфера
            Vector3f screenV1 = new Vector3f(p1.getX(), p1.getY(), v1Clip.getZ());
            Vector3f screenV2 = new Vector3f(p2.getX(), p2.getY(), v2Clip.getZ());
            Vector3f screenV3 = new Vector3f(p3.getX(), p3.getY(), v3Clip.getZ());

            // Simple clipping (отсечение если всё сзади)
            if (v1Clip.getZ() > 1.0 && v2Clip.getZ() > 1.0 && v3Clip.getZ() > 1.0) continue;
            if (v1Clip.getZ() < -1.0 && v2Clip.getZ() < -1.0 && v3Clip.getZ() < -1.0) continue;

            // Вызов растеризатора
            rasterizer.drawTriangle(
                    pixelWriter,
                    screenV1, screenV2, screenV3,
                    uv1, uv2, uv3,
                    n1, n2, n3,
                    null, // сюда мне нужна текстура
                    new Vector3f(0, 0, 1), // light dir
                    settings
            );
        }
    }

    /**
     * Создаем матрицу "Мира" для камеры, чтобы отобразить её как 3D объект.
     * Мы не меняем поля внутри Model (translation/rotation), так как модель одна на всех.
     * Мы создаем матрицу "на лету".
     */
    private Matrix4x4 createCameraWorldMatrix(Camera camera) {
        Vector3f pos = camera.getPosition();
        Vector3f target = camera.getTarget();
        Vector3f up = new Vector3f(0, 1, 0);

        // Z-ось модели камеры смотрит на цель
        Vector3f forward = target.subtract(pos).normalizeSafe();
        Vector3f right = forward.cross(up).normalizeSafe();
        Vector3f trueUp = right.cross(forward).normalizeSafe();

        Matrix4x4 matrix = new Matrix4x4();

        // Вращение (базисные вектора)
        matrix.set(0, 0, right.getX());   matrix.set(0, 1, trueUp.getX());   matrix.set(0, 2, forward.getX());
        matrix.set(1, 0, right.getY());   matrix.set(1, 1, trueUp.getY());   matrix.set(1, 2, forward.getY());
        matrix.set(2, 0, right.getZ());   matrix.set(2, 1, trueUp.getZ());   matrix.set(2, 2, forward.getZ());

        // Позиция
        matrix.set(0, 3, pos.getX());
        matrix.set(1, 3, pos.getY());
        matrix.set(2, 3, pos.getZ());

        matrix.set(3, 3, 1.0f);

        return matrix;
    }
}