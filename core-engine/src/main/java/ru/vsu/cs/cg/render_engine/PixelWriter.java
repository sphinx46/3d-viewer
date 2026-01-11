package ru.vsu.cs.cg.render_engine;

import javafx.scene.paint.Color;

@FunctionalInterface
public interface PixelWriter {
    /**
     * Устанавливает цвет пикселя в заданных координатах.
     * @param x координата X
     * @param y координата Y
     * @param color цвет пикселя (JavaFX Color)
     */
    void setPixel(int x, int y, Color color);
}