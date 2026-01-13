package ru.vsu.cs.cg.renderEngine.dto;

import javafx.scene.paint.Color;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.rasterization.Texture;

/**
 * DTO для передачи данных о рендеринге из SceneObject в RenderEngine.
 * Разрывает зависимость ядра от сцены.
 */
public class RenderEntity {
    private final Model model;
    private final Vector3f translation;
    private final Vector3f rotation;
    private final Vector3f scale;
    private final Texture texture;
    private final Color color;
    private final boolean useTexture;
    private final boolean useLighting;

    public RenderEntity(Model model, Vector3f translation, Vector3f rotation, Vector3f scale,
                        Texture texture, Color color, boolean useTexture, boolean useLighting) {
        this.model = model;
        this.translation = translation;
        this.rotation = rotation;
        this.scale = scale;
        this.texture = texture;
        this.color = color;
        this.useTexture = useTexture;
        this.useLighting = useLighting;
    }

    public Model getModel() { return model; }
    public Vector3f getTranslation() { return translation; }
    public Vector3f getRotation() { return rotation; }
    public Vector3f getScale() { return scale; }
    public Texture getTexture() { return texture; }
    public Color getColor() { return color; }
    public boolean isUseTexture() { return useTexture; }
    public boolean isUseLighting() { return useLighting; }

    @Override
    public String toString(){
        return "Это модель для рендера";
    }
}
