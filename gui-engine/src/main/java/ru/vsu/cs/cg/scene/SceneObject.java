package ru.vsu.cs.cg.scene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.rasterization.Texture;

import java.util.UUID;

public class SceneObject {
    private static final Logger LOG = LoggerFactory.getLogger(SceneObject.class);

    private final String id;
    private String name;
    private Model model;
    private Transform transform;
    private Texture texture;
    private Material material;
    private boolean visible;
    private RasterizerSettings renderSettings;

    @JsonCreator
    public SceneObject(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("model") Model model,
        @JsonProperty("transform") Transform transform,
        @JsonProperty("material") Material material,
        @JsonProperty("visible") boolean visible,
        @JsonProperty("renderSettings") RasterizerSettings renderSettings) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name != null ? name : generateDefaultName();
        this.model = model;
        this.transform = transform != null ? transform : new Transform();
        this.material = material != null ? material : new Material();
        this.visible = visible;
        this.renderSettings = renderSettings != null ? renderSettings : new RasterizerSettings();
        LOG.debug("Создан SceneObject: id={}, name={}, visible={}", id, name, visible);
    }

    public SceneObject(String name, Model model) {
        this(UUID.randomUUID().toString(), name, model, new Transform(), new Material(), true, null);
    }

    public SceneObject(Model model) {
        this(generateDefaultName(), model);
    }

    private static String generateDefaultName() {
        return "Object_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) {
        String oldName = this.name;
        this.name = name != null ? name : generateDefaultName();
        LOG.debug("Имя объекта изменено: '{}' -> '{}'", oldName, this.name);
    }

    public Model getModel() { return model; }
    public void setModel(Model model) {
        this.model = model;
        LOG.debug("Модель объекта '{}' обновлена", name);
    }

    public Transform getTransform() { return transform; }
    public void setTransform(Transform transform) { this.transform = transform; }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) {
        boolean oldValue = this.visible;
        this.visible = visible;
        LOG.debug("Видимость объекта '{}' изменена: {} -> {}", name, oldValue, visible);
    }

    public RasterizerSettings getRenderSettings() { return renderSettings; }
    public void setRenderSettings(RasterizerSettings renderSettings) {
        this.renderSettings = renderSettings != null ? renderSettings : new RasterizerSettings();
        LOG.debug("Настройки рендеринга объекта '{}' обновлены", name);
    }

    public SceneObject copy() {
        RasterizerSettings copiedSettings = renderSettings.copy();
        SceneObject copy = new SceneObject(
            UUID.randomUUID().toString(),
            name + "_copy",
            model,
            new Transform(
                transform.getPositionX(), transform.getPositionY(), transform.getPositionZ(),
                transform.getRotationX(), transform.getRotationY(), transform.getRotationZ(),
                transform.getScaleX(), transform.getScaleY(), transform.getScaleZ()
            ),
            new Material(
                material.getRed(), material.getGreen(), material.getBlue(), material.getAlpha(),
                material.getTexturePath(),
                material.getShininess(), material.getReflectivity(), material.getTransparency()
            ),
            visible,
            copiedSettings
        );
        LOG.debug("Создана копия объекта '{}' с id={}", name, copy.getId());
        return copy;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public String toString() {
        return String.format("SceneObject{id='%s', name='%s', visible=%s}", id, name, visible);
    }
}
