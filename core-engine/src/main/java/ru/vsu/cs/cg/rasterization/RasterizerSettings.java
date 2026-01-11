package ru.vsu.cs.cg.rasterization;

import javafx.scene.paint.Color;

/**
 * класс для настройки растеризатора,
 * управляющий параметрами отрисовки треугольника
 */
public class RasterizerSettings {
    private boolean useTexture;
    private boolean useLighting;
    private Color defaultColor;

    public RasterizerSettings(boolean useTexture, boolean useLighting, Color defaultColor) {
        this.useTexture = useTexture;
        this.useLighting = useLighting;
        this.defaultColor = defaultColor;
    }

    public RasterizerSettings(){
        this.useTexture = false;
        this.useLighting = false;
        this.defaultColor = Color.GRAY;
    }

    public boolean isUseTexture() { return useTexture; }
    public void setUseTexture(boolean useTexture) { this.useTexture = useTexture; }

    public boolean isUseLighting() { return useLighting; }
    public void setUseLighting(boolean useLighting) { this.useLighting = useLighting; }

    public Color getDefaultColor() { return defaultColor; }
    public void setDefaultColor(Color defaultColor) { this.defaultColor = defaultColor; }
}