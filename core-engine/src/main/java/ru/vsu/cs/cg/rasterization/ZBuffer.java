package ru.vsu.cs.cg.rasterization;

import java.util.Arrays;

public class ZBuffer {
    private float[] buffer;
    private int width;
    private int height;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new float[width * height];
        clear();
    }

    /**
     * Очистка буфера и заполнение его максимальными значениями
     */
    public void clear() {
        Arrays.fill(buffer, Float.MAX_VALUE);
    }


    /**
     * Проверка можно ли рисовать пиксель
     *
     * @param x горизонтальная координата
     * @param y вертикальная координата
     * @param z глубина
     * @return true если можно отрисовывать пиксель, иначе false
     */
    public boolean checkAndSet(int x, int y, float z) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }

        int index = y * width + x;

        if (z < buffer[index]) {
            buffer[index] = z;
            return true;
        }

        return false;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}