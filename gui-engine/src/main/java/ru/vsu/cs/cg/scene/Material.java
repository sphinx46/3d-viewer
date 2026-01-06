package ru.vsu.cs.cg.scene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.json.deserializers.ColorDeserializer;
import ru.vsu.cs.cg.json.serializers.ColorSerializer;

@JsonSerialize
@JsonDeserialize
public class Material {
    private static final Logger LOG = LoggerFactory.getLogger(Material.class);

    private double red;
    private double green;
    private double blue;
    private double alpha;
    private String texturePath;
    private double shininess;
    private double reflectivity;
    private double transparency;

    @JsonCreator
    public Material(
        @JsonProperty("red") double red,
        @JsonProperty("green") double green,
        @JsonProperty("blue") double blue,
        @JsonProperty("alpha") double alpha,
        @JsonProperty("texturePath") String texturePath,
        @JsonProperty("shininess") double shininess,
        @JsonProperty("reflectivity") double reflectivity,
        @JsonProperty("transparency") double transparency) {
        this.red = Math.max(0, Math.min(1, red));
        this.green = Math.max(0, Math.min(1, green));
        this.blue = Math.max(0, Math.min(1, blue));
        this.alpha = Math.max(0, Math.min(1, alpha));
        this.texturePath = texturePath;
        this.shininess = Math.max(0, Math.min(1, shininess));
        this.reflectivity = Math.max(0, Math.min(1, reflectivity));
        this.transparency = Math.max(0, Math.min(1, transparency));
        LOG.debug("Создан Material: color({}, {}, {}, {}), texture: {}, shininess: {}, reflectivity: {}, transparency: {}",
            red, green, blue, alpha, texturePath, shininess, reflectivity, transparency);
    }

    public Material() {
        this(0.8, 0.8, 0.8, 1.0, null, 0.5, 0.2, 0.0);
    }

    @JsonSerialize(using = ColorSerializer.class)
    public Color getColor() {
        return Color.color(red, green, blue, alpha);
    }

    @JsonDeserialize(using = ColorDeserializer.class)
    public void setColor(Color color) {
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        this.alpha = color.getOpacity();
    }

    public double getRed() { return red; }
    public void setRed(double red) { this.red = Math.max(0, Math.min(1, red)); }

    public double getGreen() { return green; }
    public void setGreen(double green) { this.green = Math.max(0, Math.min(1, green)); }

    public double getBlue() { return blue; }
    public void setBlue(double blue) { this.blue = Math.max(0, Math.min(1, blue)); }

    public double getAlpha() { return alpha; }
    public void setAlpha(double alpha) { this.alpha = Math.max(0, Math.min(1, alpha)); }

    public String getTexturePath() { return texturePath; }
    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }

    public double getShininess() { return shininess; }
    public void setShininess(double shininess) { this.shininess = Math.max(0, Math.min(1, shininess)); }

    public double getReflectivity() { return reflectivity; }
    public void setReflectivity(double reflectivity) { this.reflectivity = Math.max(0, Math.min(1, reflectivity)); }

    public double getTransparency() { return transparency; }
    public void setTransparency(double transparency) { this.transparency = Math.max(0, Math.min(1, transparency)); }

    public void reset() {
        red = green = blue = 0.8;
        alpha = 1.0;
        texturePath = null;
        shininess = 0.5;
        reflectivity = 0.2;
        transparency = 0.0;
        LOG.debug("Material сброшен к значениям по умолчанию");
    }
}
