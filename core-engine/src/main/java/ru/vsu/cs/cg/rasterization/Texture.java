package ru.vsu.cs.cg.rasterization;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * Класс для хранения текстуры и наложения ее на 3D модель
 * будет использоваться при наложении на 3D модель
 */

public class Texture {
    private Image image;
    private PixelReader reader;
    private double width;
    private double height;

    public Texture(String url) {
        if (url == null){
            throw new NullPointerException("URL текстуры не может быть null");
        }

        this.image = new Image(url);
        this.reader = image.getPixelReader();

        if (this.reader == null){
            throw new IllegalStateException("Не удалось получить PixelReader текстуры");
        }

        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    /**
     * Возвращает цвет пикселя текстуры по заданным UV координатам
     * @param u горизонтальная координата текстуры в диапазоне от 0 до 1
     * @param v вертикальная координата текстуры в диапазона от 0 до 1
     * @return цвет пикселя в указанных координатах текстуры
     */

    public Color getPixel(float u, float v) {
        if (u < 0) u = 0;
        if (u > 1) u = 1;
        if (v < 0) v = 0;
        if (v > 1) v = 1;

        int x = (int) (u * (width - 1));
        int y = (int) ((1 - v) * (height - 1));

        if (reader != null) {
            return reader.getColor(x, y);
        }
        return Color.WHITE;
    }
}