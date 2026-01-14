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
    private Color gridColor;
    private Color defaultColor;

    public RasterizerSettings(boolean useTexture, boolean useLighting,
                              boolean drawPolygonalGrid, boolean drawAxisLines,
                              Color defaultColor, Color gridColor) {
        this.useTexture = useTexture;
        this.useLighting = useLighting;
        this.drawPolygonalGrid = drawPolygonalGrid;
        this.drawAxisLines = drawAxisLines;
        this.defaultColor = defaultColor;
        this.gridColor = gridColor;
    }

    public RasterizerSettings() {
        this.useTexture = false;
        this.useLighting = false;
        this.drawPolygonalGrid = false;
        this.drawAxisLines = false;
        this.defaultColor = Color.GRAY;
        this.gridColor = Color.BLACK;
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

    public RasterizerSettings copy() {
        return new RasterizerSettings(
            this.isUseTexture(),
            this.isUseLighting(),
            this.isDrawPolygonalGrid(),
            this.isDrawAxisLines(),
            this.getDefaultColor(),
            this.getGridColor());
    }
}
