package ru.vsu.cs.cg.renderEngine.dto;

import javafx.scene.paint.Color;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.rasterization.Texture;

/**
 * DTO для передачи данных о рендеринге из SceneObject в RenderEngine.
 */
public class RenderEntity {
    private final Model model;
    private final Vector3f translation;
    private final Vector3f rotation;
    private final Vector3f scale;
    private final Texture texture;
    private final RasterizerSettings settings;

    public RenderEntity(Model model, Vector3f translation, Vector3f rotation, Vector3f scale,
                        Texture texture, RasterizerSettings settings) {
        this.model = model;
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
        this.texture = texture;
        this.settings = settings.copy();


    }

    public Model getModel() { return model; }
    public Vector3f getTranslation() { return translation; }
    public Vector3f getRotation() { return rotation; }
    public Vector3f getScale() { return scale; }
    public Texture getTexture() { return texture; }
    public Color getColor() { return settings.getDefaultColor(); }
    public RasterizerSettings getSettings(){return this.settings;}
    public boolean isUseTexture() { return settings.isUseTexture(); }
    public boolean isUseLighting() { return settings.isUseLighting(); }
    public boolean isDrawPolygonalGrid() { return settings.isDrawPolygonalGrid(); }


    @Override
    public String toString(){
        return "Это модель для рендера";
    }
}
