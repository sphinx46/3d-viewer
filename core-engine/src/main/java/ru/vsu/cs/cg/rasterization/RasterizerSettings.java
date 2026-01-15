package ru.vsu.cs.cg.rasterization;

import javafx.scene.paint.Color;

/**
 * класс для настройки растеризатора,
 * управляющий параметрами отрисовки треугольника
 */
public class RasterizerSettings {
    private boolean useTexture;
    private boolean useLighting;
    private boolean drawPolygonalGrid;
    private boolean drawAxisLines;
    private boolean drawGrid;

    private float ambientStrength = 0.3f;
    private float diffuseStrength = 1.0f;
    private float lightIntensity = 1.5f;

    private Color gridColor;
    private Color defaultColor;
    private Color lightColor;

    public RasterizerSettings(boolean useTexture, boolean useLighting,
                              boolean drawPolygonalGrid, boolean drawAxisLines, boolean drawGrid,
                              float ambientStrength, float diffuseStrength, float lightIntensity,
                              Color defaultColor, Color gridColor, Color lightColor) {
        this.useTexture = useTexture;
        this.useLighting = useLighting;
        this.drawPolygonalGrid = drawPolygonalGrid;
        this.drawAxisLines = drawAxisLines;
        this.drawGrid = drawGrid;
        this.ambientStrength = ambientStrength;
        this.diffuseStrength = diffuseStrength;
        this.lightIntensity = lightIntensity;
        this.defaultColor = defaultColor;
        this.gridColor = gridColor;
        this.lightColor = lightColor;
    }

    public RasterizerSettings() {
        this.useTexture = false;
        this.useLighting = false;
        this.drawPolygonalGrid = false;
        this.drawAxisLines = false;
        this.drawGrid = false;
        this.defaultColor = Color.GRAY;
        this.gridColor = Color.BLACK;
        this.lightColor = Color.WHITE;
    }

    public Color getGridColor() { return gridColor; }
    public void setGridColor(Color gridColor) { this.gridColor = gridColor; }

    public boolean isDrawPolygonalGrid() { return drawPolygonalGrid; }
    public void setDrawPolygonalGrid(boolean drawPolygonalGrid) {
        this.drawPolygonalGrid = drawPolygonalGrid;
    }

    public boolean isUseTexture() { return useTexture; }
    public void setUseTexture(boolean useTexture) { this.useTexture = useTexture; }

    public boolean isUseLighting() { return useLighting; }
    public void setUseLighting(boolean useLighting) { this.useLighting = useLighting; }

    public Color getDefaultColor() { return defaultColor; }
    public void setDefaultColor(Color defaultColor) { this.defaultColor = defaultColor; }

    public boolean isDrawAxisLines() { return drawAxisLines; }
    public void setDrawAxisLines(boolean drawAxisLines) {
        this.drawAxisLines = drawAxisLines;
    }

    public boolean isDrawGrid() {return drawGrid;}
    public void setDrawGrid(boolean drawGrid) {this.drawGrid = drawGrid;}

    public float getAmbientStrength() { return ambientStrength; }
    public void setAmbientStrength(float value) { this.ambientStrength = value; }

    public float getDiffuseStrength() { return diffuseStrength; }
    public void setDiffuseStrength(float value) { this.diffuseStrength = value; }

    public float getLightIntensity() { return lightIntensity; }
    public void setLightIntensity(float value) { this.lightIntensity = value; }

    public Color getLightColor() {return lightColor;}
    public void setLightColor(Color lightColor) {this.lightColor = lightColor;}

    public RasterizerSettings copy() {
        return new RasterizerSettings(
            this.isUseTexture(),
            this.isUseLighting(),
            this.isDrawPolygonalGrid(),
            this.isDrawAxisLines(),
            this.isDrawGrid(),
            this.getAmbientStrength(),
            this.getDiffuseStrength(),
            this.getLightIntensity(),
            this.getDefaultColor(),
            this.getGridColor(),
                this.getLightColor()
        );
    }
}
