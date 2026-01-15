package ru.vsu.cs.cg.scene;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.rasterization.Rasterizer;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.rasterization.Texture;
import ru.vsu.cs.cg.rasterization.ZBuffer;
import ru.vsu.cs.cg.renderEngine.PixelWriter;
import ru.vsu.cs.cg.renderEngine.RenderEngine;
import ru.vsu.cs.cg.renderEngine.dto.RenderEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер сцены, отвечающий за управление 3D сценой, камерами и процессом рендеринга.
 * Обрабатывает загрузку текстур, преобразование объектов сцены в сущности для рендеринга,
 * управление буферами и координацию работы рендеринга.
 */
public class SceneManager {
    private static final Logger LOG = LoggerFactory.getLogger(SceneManager.class);

    private Scene scene;
    private final List<Camera> cameras = new ArrayList<>();
    private Camera activeCamera;

    private final RenderEngine renderEngine;
    private Rasterizer rasterizer;
    private ZBuffer zBuffer;
    private RasterizerSettings renderSettings;

    private int width = 800;
    private int height = 600;

    private final Map<String, Texture> textureCache = new HashMap<>();

    public SceneManager() {
        this.scene = new Scene();
        this.renderEngine = new RenderEngine();
        this.renderSettings = new RasterizerSettings();
        initBuffers(width, height);

        LOG.info("SceneManager создан");
    }

    /**
     * Инициализирует буферы глубины и растеризатор для заданного размера.
     *
     * @param width  Ширина буфера
     * @param height Высота буфера
     */
    private void initBuffers(int width, int height) {
        this.zBuffer = new ZBuffer(width, height);
        this.rasterizer = new Rasterizer(zBuffer);
    }

    /**
     * Изменяет размер области рендеринга и обновляет соответствующие буферы и камеры.
     *
     * @param width  Новая ширина области рендеринга
     * @param height Новая высота области рендеринга
     */
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        this.width = width;
        this.height = height;

        LOG.debug("SceneManager получил запрос на изменение размера: {}x{}", width, height);

        initBuffers(width, height);

        float aspectRatio = (float) width / height;
        for (Camera camera : cameras) {
            camera.setAspectRatio(aspectRatio);
        }
    }

    /**
     * Выполняет рендеринг всей сцены в буфер пикселей.
     * Преобразует объекты сцены в RenderEntity, применяет материалы и текстуры,
     * и делегирует рендеринг движку.
     *
     * @param pixelWriter Писатель пикселей, в который записывается результат рендеринга
     */
    public void render(PixelWriter pixelWriter) {
        if (activeCamera == null && !cameras.isEmpty()) {
            setActiveCamera(cameras.get(0));
        }

        if (activeCamera == null) {
            LOG.warn("Нет активной камеры для рендеринга");
            return;
        }

        zBuffer.clear();

        List<RenderEntity> renderEntities = new ArrayList<>();

        for (SceneObject object : scene.getObjects()) {
            if (!object.isVisible()) continue;

            Transform t = object.getTransform();
            Vector3f translation = new Vector3f((float) t.getPositionX(), (float) t.getPositionY(), (float) t.getPositionZ());
            Vector3f rotation = new Vector3f((float) t.getRotationX(), (float) t.getRotationY(), (float) t.getRotationZ());
            Vector3f scale = new Vector3f((float) t.getScaleX(), (float) t.getScaleY(), (float) t.getScaleZ());

            Material m = object.getMaterial();
            Texture texture = null;

            if (m.getTexturePath() != null && !m.getTexturePath().isEmpty()) {
                texture = getOrLoadTexture(m.getTexturePath());
            }

            RasterizerSettings objectRenderSettings = object.getRenderSettings();
            objectRenderSettings.setDefaultColor(m.getColor());
            objectRenderSettings.setLightIntensity((float) m.getLightIntensity());
            objectRenderSettings.setAmbientStrength((float) m.getDiffusion());
            objectRenderSettings.setDiffuseStrength((float) m.getAmbient());

            RenderEntity entity = new RenderEntity(
                object.getModel(),
                translation,
                rotation,
                scale,
                texture,
                objectRenderSettings
            );

            renderEntities.add(entity);
        }

        renderSettings.setDrawGrid(scene.isGridVisible());
        renderEngine.render(
            pixelWriter,
            width,
            height,
            renderEntities,
            cameras,
            activeCamera,
            rasterizer,
            renderSettings
        );
    }

    /**
     * Получает текстуру из кэша или загружает ее из файла.
     * Использует кэширование для избежания повторной загрузки одних и тех же текстур.
     *
     * @param path Путь к файлу текстуры
     * @return Загруженная текстура или null, если загрузка не удалась
     */
    private Texture getOrLoadTexture(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }

        try {
            File file = new File(path);

            if (!file.exists()) {
                LOG.warn("Файл текстуры не найден: {}", path);
                return null;
            }

            String url = file.toURI().toString();
            Image image = new Image(url);

            if (image.isError()) {
                LOG.error("Ошибка загрузки текстуры: {}", path);
                return null;
            }

            Texture texture = new Texture(image);
            textureCache.put(path, texture);

            LOG.debug("Текстура загружена в кэш: {}", path);
            return texture;

        } catch (Exception e) {
            LOG.error("Не удалось загрузить текстуру: {}", path, e);
            return null;
        }
    }

    /**
     * Добавляет камеру в сцену.
     */
    public void addCamera(Camera camera) {
        if (camera != null && !cameras.contains(camera)) {
            cameras.add(camera);
            camera.setAspectRatio((float) width / height);
            if (activeCamera == null) activeCamera = camera;
        }
    }

    /**
     * Удаляет камеру из сцены.
     * Нельзя удалить последнюю камеру в сцене.
     *
     * @param camera Камера для удаления
     * @throws IllegalStateException если попытка удалить последнюю камеру
     */
    public void removeCamera(Camera camera) {
        if (camera == null) return;

        if (cameras.size() <= 1) {
            throw new IllegalStateException("Нельзя удалить единственную камеру в сцене.");
        }

        cameras.remove(camera);

        if (activeCamera == camera) {
            activeCamera = cameras.isEmpty() ? null : cameras.get(0);
        }
        LOG.info("Камера '{}' удалена", camera.getId());
    }

    public void setRenderSettings(RasterizerSettings settings) {
        this.renderSettings = settings;
    }

    public RasterizerSettings getRenderSettings() {
        return renderSettings;
    }

    /**
     * Устанавливает активную камеру для рендеринга.
     * Если камера не была добавлена в сцену, она добавляется автоматически.
     *
     * @param camera Камера для установки активной
     */
    public void setActiveCamera(Camera camera) {
        if (cameras.contains(camera)) {
            activeCamera = camera;
        } else {
            addCamera(camera);
            activeCamera = camera;
        }
    }

    public Camera getActiveCamera() {
        return activeCamera;
    }

    public List<Camera> getCameras() {
        return new ArrayList<>(cameras);
    }

    public boolean isActiveCamera(String cameraId) {
        return activeCamera != null && activeCamera.getId().equals(cameraId);
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        LOG.info("Сцена установлена в SceneManager: {}", scene.getName());
    }

    public void clearScene() {
        scene.clear();
        textureCache.clear();
        LOG.info("Сцена очищена через SceneManager");
    }

    public int getObjectCount() {
        return scene.getObjectCount();
    }

    public boolean isEmpty() {
        return scene.isEmpty();
    }
}
