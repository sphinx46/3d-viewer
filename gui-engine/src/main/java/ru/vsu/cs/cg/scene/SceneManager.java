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

    private void initBuffers(int width, int height) {
        this.zBuffer = new ZBuffer(width, height);
        this.rasterizer = new Rasterizer(zBuffer);
    }

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

    public void render(PixelWriter pixelWriter) {
        if (activeCamera == null) {
            setActiveCamera(cameras.get(0));
        }

        zBuffer.clear();

        List<RenderEntity> renderEntities = prepareRenderEntities();
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

    private List<RenderEntity> prepareRenderEntities() {
        List<RenderEntity> renderEntities = new ArrayList<>();
        for (SceneObject object : scene.getObjects()) {
            if (!object.isVisible()) continue;
            renderEntities.add(createRenderEntity(object));
        }
        return renderEntities;
    }

    private RenderEntity createRenderEntity(SceneObject object) {
        Transform t = object.getTransform();
        Vector3f translation = new Vector3f((float) t.getPositionX(), (float) t.getPositionY(), (float) t.getPositionZ());
        Vector3f rotation = new Vector3f((float) t.getRotationX(), (float) t.getRotationY(), (float) t.getRotationZ());
        Vector3f scale = new Vector3f((float) t.getScaleX(), (float) t.getScaleY(), (float) t.getScaleZ());

        Material m = object.getMaterial();
        Texture texture = getOrLoadTexture(m.getTexturePath());

        RasterizerSettings objectRenderSettings = object.getRenderSettings();
        boolean useTexture = (texture != null) && objectRenderSettings.isUseTexture();
        boolean useLighting = objectRenderSettings.isUseLighting();
        boolean drawPolygonalGrid = objectRenderSettings.isDrawPolygonalGrid();

        return new RenderEntity(
            object.getModel(),
            translation,
            rotation,
            scale,
            texture,
            m.getColor(),
            useTexture,
            useLighting,
            drawPolygonalGrid
        );
    }

    private Texture getOrLoadTexture(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }

        try {
            String url;
            File file = new File(path);

            if (file.exists()) {
                url = file.toURI().toString();
            } else {
                url = path;
            }

            Image image = new Image(url);

            if (image.isError()) {
                if (image.getException() != null) {
                    throw image.getException();
                }
                throw new Exception("Некорректный формат изображения или ошибка чтения");
            }

            Texture texture = new Texture(image);
            textureCache.put(path, texture);
            return texture;

        } catch (Exception e) {
            LOG.error("Не удалось загрузить текстуру: " + path, e);
            return null;
        }
    }

    public void addCamera(Camera camera) {
        if (camera != null && !cameras.contains(camera)) {
            cameras.add(camera);
            camera.setAspectRatio((float) width / height);
            if (activeCamera == null) activeCamera = camera;
        }
    }

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
