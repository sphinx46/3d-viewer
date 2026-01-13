package ru.vsu.cs.cg.utils.adapter;

import javafx.scene.paint.Color;
import ru.vsu.cs.cg.renderEngine.PixelWriter; // Импортируем интерфейс ДВИЖКА

public class JavaFXPixelWriterAdapter implements PixelWriter {
    private final javafx.scene.image.PixelWriter fxPixelWriter;

    public JavaFXPixelWriterAdapter(javafx.scene.image.PixelWriter fxPixelWriter) {
        this.fxPixelWriter = fxPixelWriter;
    }

    @Override
    public void setPixel(int x, int y, Color color) {
        if (color != null) {
            fxPixelWriter.setColor(x, y, color);
        }
    }
}