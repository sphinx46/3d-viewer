package ru.vsu.cs.cg.scene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SceneObject {
    private static final Logger LOG = LoggerFactory.getLogger(SceneObject.class);

    private final String id;
    private String name;
    private Model model;
    private Transform transform;
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
        LOG.debug("Создан SceneObject: id={}, name={}", id, name);
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
        this.name = name != null ? name : generateDefaultName();
    }

    public Model getModel() { return model; }
    public void setModel(Model model) {
        this.model = model;
    }

    public Transform getTransform() { return transform; }
    public void setTransform(Transform transform) { this.transform = transform; }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public RasterizerSettings getRenderSettings() { return renderSettings; }
    public void setRenderSettings(RasterizerSettings renderSettings) {
        this.renderSettings = renderSettings != null ? renderSettings : new RasterizerSettings();
    }

    public SceneObject copy() {
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
            renderSettings.copy()
        );
        return copy;
    }

    public Model getTransformedModel() {
        ru.vsu.cs.cg.math.Vector3f translation = new ru.vsu.cs.cg.math.Vector3f(
            (float) transform.getPositionX(),
            (float) transform.getPositionY(),
            (float) transform.getPositionZ()
        );

        ru.vsu.cs.cg.math.Vector3f rotation = new ru.vsu.cs.cg.math.Vector3f(
            (float) transform.getRotationX(),
            (float) transform.getRotationY(),
            (float) transform.getRotationZ()
        );

        ru.vsu.cs.cg.math.Vector3f scale = new ru.vsu.cs.cg.math.Vector3f(
            (float) transform.getScaleX(),
            (float) transform.getScaleY(),
            (float) transform.getScaleZ()
        );

        Model transformedModel = model.createTransformedCopy(translation, rotation, scale);

        if (material.getTexturePath() != null) {
            transformedModel.setTexturePath(material.getTexturePath());
        }

        if (renderSettings.isUseLighting()) {
            transformedModel.setMaterialShininess((float) material.getShininess());
        }

        transformedModel.setMaterialColor(new float[]{
            (float) material.getRed(),
            (float) material.getGreen(),
            (float) material.getBlue()
        });

        transformedModel.setMaterialTransparency((float) material.getTransparency());
        transformedModel.setMaterialReflectivity((float) material.getReflectivity());

        transformedModel.setUseLighting(renderSettings.isUseLighting());
        transformedModel.setUseTexture(renderSettings.isUseTexture() || material.getTexturePath() != null);
        transformedModel.setDrawPolygonalGrid(renderSettings.isDrawPolygonalGrid());

        return transformedModel;
    }

    @Override
    public String toString() {
        return String.format("SceneObject{id='%s', name='%s', visible=%s}", id, name, visible);
    }
}
