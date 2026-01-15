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
    private double lightIntensity;
    private double diffusion;
    private double ambient;

    @JsonCreator
    public Material(
        @JsonProperty("red") double red,
        @JsonProperty("green") double green,
        @JsonProperty("blue") double blue,
        @JsonProperty("alpha") double alpha,
        @JsonProperty("texturePath") String texturePath,
        @JsonProperty("lightIntensity") double lightIntensity,
        @JsonProperty("diffusion") double diffusion,
        @JsonProperty("ambient") double ambient) {
        this.red = Math.max(0, Math.min(1, red));
        this.green = Math.max(0, Math.min(1, green));
        this.blue = Math.max(0, Math.min(1, blue));
        this.alpha = Math.max(0, Math.min(1, alpha));
        this.texturePath = texturePath;
        this.lightIntensity = Math.max(0, Math.min(1, lightIntensity));
        this.diffusion = Math.max(0, Math.min(1, diffusion));
        this.ambient = Math.max(0, Math.min(1, ambient));
        LOG.debug("Создан Material: color({}, {}, {}, {}), texture: {}, shininess: {}, diffusion: {}, transparency: {}",
            red, green, blue, alpha, texturePath, lightIntensity, diffusion, ambient);
    }

    public Material() {
        this(0.8, 0.8, 0.8, 1.0, null, 1.0, 0.3, 1.0);
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

    public double getLightIntensity() { return lightIntensity; }
    public void setLightIntensity(double lightIntensity) { this.lightIntensity = Math.max(0, Math.min(1, lightIntensity)); }

    public double getDiffusion() { return diffusion; }
    public void setDiffusion(double diffusion) { this.diffusion = Math.max(0, Math.min(1, diffusion)); }

    public double getAmbient() { return ambient; }
    public void setAmbient(double ambient) { this.ambient = Math.max(0, Math.min(1, ambient)); }

    public void reset() {
        red = green = blue = 0.8;
        alpha = 1.0;
        texturePath = null;
        lightIntensity = 0.5;
        diffusion = 0.2;
        ambient = 0.0;
        LOG.debug("Material сброшен к значениям по умолчанию");
    }
}
