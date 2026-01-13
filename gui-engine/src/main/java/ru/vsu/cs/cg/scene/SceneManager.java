package ru.vsu.cs.cg.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.rasterization.Rasterizer;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.rasterization.Texture;
import ru.vsu.cs.cg.rasterization.ZBuffer;
import ru.vsu.cs.cg.renderEngine.PixelWriter;
import ru.vsu.cs.cg.renderEngine.RenderEngine;
import ru.vsu.cs.cg.renderEngine.dto.RenderEntity;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneManager {
    private static final Logger LOG = LoggerFactory.getLogger(SceneManager.class);

    private Scene scene;
    private final List<Camera> cameras = new ArrayList<>();
    private Camera activeCamera;

    // Компоненты движка рендеринга
    private final RenderEngine renderEngine;
    private Rasterizer rasterizer;
    private ZBuffer zBuffer;
    private RasterizerSettings renderSettings;

    // Параметры окна
    private int width = 800;
    private int height = 600;

    // Кэш текстур (Path -> Texture Object)
    private final Map<String, Texture> textureCache = new HashMap<>();

    public SceneManager() {
        this.scene = new Scene();
        this.renderEngine = new RenderEngine();
        this.renderSettings = new RasterizerSettings();
        renderSettings.setDrawPolygonalGrid(true);

        // Инициализация буферов
        initBuffers(width, height);

        LOG.info("SceneManager создан");
    }

    private void initBuffers(int width, int height) {
        this.zBuffer = new ZBuffer(width, height);
        this.rasterizer = new Rasterizer(zBuffer);
    }

    // --- Методы Render/Resize (заполнение TODO) ---

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        this.width = width;
        this.height = height;

        LOG.debug("SceneManager получил запрос на изменение размера: {}x{}", width, height);

        // Пересоздаем буферы под новый размер
        initBuffers(width, height);

        // Обновляем соотношение сторон у всех камер
        float aspectRatio = (float) width / height;
        for (Camera camera : cameras) {
            camera.setAspectRatio(aspectRatio);
        }
    }

    /**
     * Основной метод отрисовки сцены на Canvas
     */
    public void render(PixelWriter pixelWriter) {
        // Проверка камеры
        if (activeCamera == null) {
            if (!cameras.isEmpty()) {
                setActiveCamera(cameras.get(0));
            } else {
                Camera camera = new Camera();
                cameras.add(camera);
                setActiveCamera(camera);
            }
        }

        // Очистка Z-буфера
        zBuffer.clear();

        // Подготовка списка сущностей для рендера (Mapping: SceneObject -> RenderEntity)
        List<RenderEntity> renderEntities = new ArrayList<>();

        for (SceneObject object : scene.getObjects()) {
            if (!object.isVisible()) continue;

            // 1. Получаем трансформацию (конвертируем double -> float)
            Transform t = object.getTransform();
            Vector3f translation = new Vector3f((float) t.getPositionX(), (float) t.getPositionY(), (float) t.getPositionZ());
            Vector3f rotation = new Vector3f((float) t.getRotationX(), (float) t.getRotationY(), (float) t.getRotationZ());
            Vector3f scale = new Vector3f((float) t.getScaleX(), (float) t.getScaleY(), (float) t.getScaleZ());

            // 2. Работа с материалом и текстурой
            Material m = object.getMaterial();
            Texture texture = null;

            // Если путь к текстуре задан, пытаемся достать её из кэша
            if (m.getTexturePath() != null && !m.getTexturePath().isEmpty()) {
                texture = getOrLoadTexture(m.getTexturePath());
            }

            boolean useTexture = (texture != null) && renderSettings.isUseTexture();

            // 3. Создаем DTO

            RenderEntity entity = new RenderEntity(
                    object.getModel(),
                    translation,
                    rotation,
                    scale,
                    texture,
                    m.getColor(),
                    useTexture,
                    renderSettings.isUseLighting()
            );

            renderEntities.add(entity);
        }

        for (RenderEntity entity: renderEntities){
            LOG.debug("{}", entity.toString());
        }

        // Вызов ядра рендеринга
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

    private Texture getOrLoadTexture(String path) {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }
        try {
            Texture texture = new Texture(path);
            textureCache.put(path, texture);
            return texture;
        } catch (Exception e) {
            LOG.error("Не удалось загрузить текстуру: " + path, e);
            return null;
        }
    }

    // --- Геттеры и сеттеры ---

    public void setRenderSettings(RasterizerSettings settings) {
        this.renderSettings = settings;
    }

    public RasterizerSettings getRenderSettings() {
        return renderSettings;
    }

    public void addCamera(Camera camera) {
        if (camera != null && !cameras.contains(camera)) {
            cameras.add(camera);
            camera.setAspectRatio((float) width / height);
            if (activeCamera == null) activeCamera = camera;
        }
    }

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
        // При смене сцены можно очистить кэш текстур, если они специфичны
        // textureCache.clear();
        LOG.info("Сцена установлена в SceneManager: {}", scene.getName());
    }

    public void clearScene() {
        scene.clear();
        textureCache.clear(); // Очищаем кэш при очистке сцены
        LOG.info("Сцена очищена через SceneManager");
    }

    public int getObjectCount() {
        return scene.getObjectCount();
    }

    public boolean isEmpty() {
        return scene.isEmpty();
    }
}
