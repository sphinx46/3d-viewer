package ru.vsu.cs.cg.rasterization;

import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.PixelWriter;
import javafx.scene.paint.Color;

/**
 * Класс растеризатора для преобразования 3D треугольников в 2D пиксели на экране.
 * Реализует алгоритм растеризации с использованием z-буфера для корректного отображения глубины.
 */
public class Rasterizer {
    private final ZBuffer zBuffer;
    private static final float EPSILON = 1e-5f;

    public Rasterizer(ZBuffer zBuffer) {
        this.zBuffer = zBuffer;
    }

    /**
     * Отрисовывает треугольник с текстурированием, освещением и интерполяцией атрибутов.
     *
     * @param pixelWriter   Писатель пикселей в буфер кадра
     * @param width         Ширина области рендеринга
     * @param height        Высота области рендеринга
     * @param vertex1       Первая вершина треугольника в экранных координатах (x, y, 1/z)
     * @param vertex2       Вторая вершина треугольника в экранных координатах (x, y, 1/z)
     * @param vertex3       Третья вершина треугольника в экранных координатах (x, y, 1/z)
     * @param uv1           Текстурные координаты первой вершины
     * @param uv2           Текстурные координаты второй вершины
     * @param uv3           Текстурные координаты третьей вершины
     * @param normal1       Нормаль первой вершины (в мировых координатах)
     * @param normal2       Нормаль второй вершины (в мировых координатах)
     * @param normal3       Нормаль третьей вершины (в мировых координатах)
     * @param texture       Текстура для наложения (может быть null)
     * @param lightDirection Направление света (нормализованный вектор)
     * @param settings      Настройки рендеринга
     */
    public void drawTriangle(
            PixelWriter pixelWriter,
            int width, int height,
            Vector3f vertex1, Vector3f vertex2, Vector3f vertex3,
            Vector2f uv1, Vector2f uv2, Vector2f uv3,
            Vector3f normal1, Vector3f normal2, Vector3f normal3,
            Texture texture,
            Vector3f lightDirection,
            RasterizerSettings settings)
    {
        if (vertex1.getY() > vertex2.getY()) {
            Vector3f tempV = vertex1; vertex1 = vertex2; vertex2 = tempV;
            Vector2f tempUV = uv1; uv1 = uv2; uv2 = tempUV;
            Vector3f tempN = normal1; normal1 = normal2; normal2 = tempN;
        }
        if (vertex1.getY() > vertex3.getY()) {
            Vector3f tempV = vertex1; vertex1 = vertex3; vertex3 = tempV;
            Vector2f tempUV = uv1; uv1 = uv3; uv3 = tempUV;
            Vector3f tempN = normal1; normal1 = normal3; normal3 = tempN;
        }
        if (vertex2.getY() > vertex3.getY()) {
            Vector3f tempV = vertex2; vertex2 = vertex3; vertex3 = tempV;
            Vector2f tempUV = uv2; uv2 = uv3; uv3 = tempUV;
            Vector3f tempN = normal2; normal2 = normal3; normal3 = tempN;
        }

        drawScanlinePart(
                pixelWriter, width, height,
                vertex1, vertex2,
                vertex1, vertex3,
                uv1, uv2,
                uv1, uv3,
                normal1, normal2,
                normal1, normal3,
                texture, lightDirection, settings
        );

        drawScanlinePart(
                pixelWriter, width, height,
                vertex2, vertex3,
                vertex1, vertex3,
                uv2, uv3,
                uv1, uv3,
                normal2, normal3,
                normal1, normal3,
                texture, lightDirection, settings
        );

        if (settings.isDrawPolygonalGrid()){
            Color gridColor = settings.getGridColor();
            drawLine(pixelWriter, width, height, vertex1, vertex2, gridColor);
            drawLine(pixelWriter, width, height, vertex2, vertex3, gridColor);
            drawLine(pixelWriter, width, height, vertex1, vertex3, gridColor);
        }
    }

    /**
     * Рендерит часть треугольника между двумя ребрами.
     * Использует алгоритм сканирующей строки с интерполяцией 1/z для perspective-correct текстурных координат.
     *
     * @param pixelWriter    Писатель пикселей
     * @param width          Ширина области рендеринга
     * @param height         Высота области рендеринга
     * @param edgeStartA     Начало первого ребра
     * @param edgeEndA       Конец первого ребра
     * @param edgeStartB     Начало второго ребра
     * @param edgeEndB       Конец второго ребра
     * @param uvStartA       UV-координаты начала первого ребра
     * @param uvEndA         UV-координаты конца первого ребра
     * @param uvStartB       UV-координаты начала второго ребра
     * @param uvEndB         UV-координаты конца второго ребра
     * @param normalStartA   Нормаль начала первого ребра
     * @param normalEndA     Нормаль конца первого ребра
     * @param normalStartB   Нормаль начала второго ребра
     * @param normalEndB     Нормаль конца второго ребра
     * @param texture        Текстура
     * @param lightDirection Направление света
     * @param settings       Настройки рендеринга
     */
    private void drawScanlinePart(
            PixelWriter pixelWriter,
            int width, int height,
            Vector3f edgeStartA, Vector3f edgeEndA,
            Vector3f edgeStartB, Vector3f edgeEndB,
            Vector2f uvStartA, Vector2f uvEndA,
            Vector2f uvStartB, Vector2f uvEndB,
            Vector3f normalStartA, Vector3f normalEndA,
            Vector3f normalStartB, Vector3f normalEndB,
            Texture texture, Vector3f lightDirection, RasterizerSettings settings
    ) {

        int scanlineYStart = (int) Math.max(0, Math.ceil(Math.max(edgeStartA.getY(), edgeStartB.getY())));
        int scanlineYEnd   = (int) Math.min(height, Math.ceil(Math.min(edgeEndA.getY(), edgeEndB.getY())));

        float depthStartA = Math.max(edgeStartA.getZ(), EPSILON);
        float depthEndA   = Math.max(edgeEndA.getZ(), EPSILON);
        float depthStartB = Math.max(edgeStartB.getZ(), EPSILON);
        float depthEndB   = Math.max(edgeEndB.getZ(), EPSILON);

        float invDepthStartA = 1.0f / depthStartA;
        float invDepthEndA   = 1.0f / depthEndA;
        float invDepthStartB = 1.0f / depthStartB;
        float invDepthEndB   = 1.0f / depthEndB;

        for (int y = scanlineYStart; y < scanlineYEnd; y++) {

            float verticalFactorA = (y - edgeStartA.getY()) / (edgeEndA.getY() - edgeStartA.getY());
            float verticalFactorB = (y - edgeStartB.getY()) / (edgeEndB.getY() - edgeStartB.getY());

            float scanlineStartX = interpolate(edgeStartA.getX(), edgeEndA.getX(), verticalFactorA);
            float scanlineEndX   = interpolate(edgeStartB.getX(), edgeEndB.getX(), verticalFactorB);

            float scanlineStartInvDepth = interpolate(invDepthStartA, invDepthEndA, verticalFactorA);
            float scanlineEndInvDepth   = interpolate(invDepthStartB, invDepthEndB, verticalFactorB);

            Vector2f uvOverDepthStart = null;
            Vector2f uvOverDepthEnd = null;
            Vector3f normalOverDepthStart = null;
            Vector3f normalOverDepthEnd = null;

            if (settings.isUseTexture() && uvStartA != null) {
                uvOverDepthStart = interpolate(uvStartA.multiply(invDepthStartA), uvEndA.multiply(invDepthEndA), verticalFactorA);
                uvOverDepthEnd   = interpolate(uvStartB.multiply(invDepthStartB), uvEndB.multiply(invDepthEndB), verticalFactorB);
            }

            if (settings.isUseLighting() && normalStartA != null) {
                normalOverDepthStart = interpolate(normalStartA.multiply(invDepthStartA), normalEndA.multiply(invDepthEndA), verticalFactorA);
                normalOverDepthEnd   = interpolate(normalStartB.multiply(invDepthStartB), normalEndB.multiply(invDepthEndB), verticalFactorB);
            }

            if (scanlineStartX > scanlineEndX) {
                float tempX = scanlineStartX; scanlineStartX = scanlineEndX; scanlineEndX = tempX;
                float tempInvDepth = scanlineStartInvDepth; scanlineStartInvDepth = scanlineEndInvDepth; scanlineEndInvDepth = tempInvDepth;
                Vector2f tempUV = uvOverDepthStart; uvOverDepthStart = uvOverDepthEnd; uvOverDepthEnd = tempUV;
                Vector3f tempNormal = normalOverDepthStart; normalOverDepthStart = normalOverDepthEnd; normalOverDepthEnd = tempNormal;
            }

            int pixelXStart = (int) Math.max(0, Math.ceil(scanlineStartX));
            int pixelXEnd   = (int) Math.min(width, Math.ceil(scanlineEndX));

            float scanlineWidth = scanlineEndX - scanlineStartX;

            if (scanlineWidth <= 0) continue;

            float invScanlineWidth = 1.0f / scanlineWidth;

            for (int x = pixelXStart; x < pixelXEnd; x++) {
                float horizontalFactor = (x - scanlineStartX) * invScanlineWidth;

                float currentInvDepth = interpolate(scanlineStartInvDepth, scanlineEndInvDepth, horizontalFactor);
                float currentPixelDepth = 1.0f / currentInvDepth;

                if (zBuffer.checkAndSet(x, y, currentPixelDepth)) {

                    Color finalPixelColor = settings.getDefaultColor();

                    if (settings.isUseTexture() && texture != null && uvOverDepthStart != null) {
                        Vector2f interpolatedUVOverDepth = interpolate(uvOverDepthStart, uvOverDepthEnd, horizontalFactor);
                        Vector2f finalUV = interpolatedUVOverDepth.multiply(currentPixelDepth);
                        finalPixelColor = texture.getPixel(finalUV.getX(), finalUV.getY());
                    }

                    if (settings.isUseLighting() && normalOverDepthStart != null) {
                        Vector3f interpolatedNormalOverDepth = interpolate(normalOverDepthStart, normalOverDepthEnd, horizontalFactor);
                        Vector3f pixelNormal = interpolatedNormalOverDepth.multiply(currentPixelDepth);
                        pixelNormal = pixelNormal.normalizeSafe();
                        float dotProduct = Math.max(0, pixelNormal.dot(lightDirection));
                        finalPixelColor = applyLight(finalPixelColor, dotProduct, settings);
                    }

                    pixelWriter.setPixel(x, y, finalPixelColor);
                }
            }
        }
    }

    /**
     * Отрисовывает 3D линию с использованием алгоритма DDA и проверкой z-буфера.
     *
     * @param pixelWriter   Писатель пикселей
     * @param width         Ширина области рендеринга
     * @param height        Высота области рендеринга
     * @param start         Начальная точка линии (x, y, 1/z)
     * @param end           Конечная точка линии (x, y, 1/z)
     * @param color         Цвет линии
     * @param ignoreZBuffer Флаг игнорирования z-буфера (true для рисования поверх всего)
     */
    public void drawLine(PixelWriter pixelWriter, int width, int height, Vector3f start, Vector3f end, Color color, boolean ignoreZBuffer) {
        float x1 = start.getX();
        float y1 = start.getY();
        float x2 = end.getX();
        float y2 = end.getY();

        if ((x1 < 0 && x2 < 0) || (x1 >= width && x2 >= width) || (y1 < 0 && y2 < 0) || (y1 >= height && y2 >= height)) return;

        float dx = x2 - x1;
        float dy = y2 - y1;
        int steps = (int) Math.max(Math.abs(dx), Math.abs(dy));

        if (steps == 0) {
            drawPixel(pixelWriter, (int)x1, (int)y1, start.getZ(), color);
            return;
        }

        float invWStart = 1.0f / Math.max(start.getZ(), EPSILON);
        float invWEnd = 1.0f / Math.max(end.getZ(), EPSILON);

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;

            int px = Math.round(x1 + dx * t);
            int py = Math.round(y1 + dy * t);

            if (px >= 0 && px < width && py >= 0 && py < height) {
                float currentInvW = invWStart + (invWEnd - invWStart) * t;
                float currentW = 1.0f / currentInvW;

                float biasedZ = currentW - 0.001f;

                if (ignoreZBuffer || zBuffer.checkAndSet(px, py, biasedZ)) {
                    pixelWriter.setPixel(px, py, color);
                }
            }
        }
    }

    /**
     * Перегрузка метода для автоматического использования z-буфера
     */
    public void drawLine(PixelWriter pixelWriter, int width, int height, Vector3f start, Vector3f end, Color color) {
        drawLine(pixelWriter, width, height, start, end, color, false);
    }

    /**
     * Отрисовывает пиксель с проверкой глубины.
     *
     * @param pw     Писатель пикселей
     * @param x      X-координата пикселя
     * @param y      Y-координата пикселя
     * @param depth  Глубина пикселя
     * @param color  Цвет пикселя
     */
    public void drawPixel(PixelWriter pw, int x, int y, float depth, Color color) {
        if (x >= 0 && x < zBuffer.getWidth() && y >= 0 && y < zBuffer.getHeight()) {
            if (zBuffer.checkAndSet(x, y, depth)) {
                pw.setPixel(x, y, color);
            }
        }
    }

    /**
     * Применяет модель освещения
     * @param baseColor базовый цвет пикселя (от текстуры или материала)
     * @param dotProduct скалярное произведение (N * L)
     * @param settings настройки, откуда берем коэффициенты
     */
    private Color applyLight(Color baseColor, float dotProduct, RasterizerSettings settings) {
        float ambient = settings.getAmbientStrength();
        float diffuse = settings.getDiffuseStrength();
        float intensity = settings.getLightIntensity();

        float brightness = (ambient + (diffuse * dotProduct)) * intensity;

        double r = Math.min(1.0, Math.max(0, baseColor.getRed() * brightness));
        double g = Math.min(1.0, Math.max(0, baseColor.getGreen() * brightness));
        double b = Math.min(1.0, Math.max(0, baseColor.getBlue() * brightness));

        return Color.color(r, g, b);
    }

    /**
     * Линейная интерполяция между двумя значениями.
     */
    private float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

    /**
     * Линейная интерполяция между двумя 2D векторами.
     */
    private Vector2f interpolate(Vector2f start, Vector2f end, float factor) {
        return new Vector2f(interpolate(start.getX(), end.getX(), factor), interpolate(start.getY(), end.getY(), factor));
    }

    /**
     * Линейная интерполяция между двумя 3D векторами.
     */
    private Vector3f interpolate(Vector3f start, Vector3f end, float factor) {
        return new Vector3f(interpolate(start.getX(), end.getX(), factor), interpolate(start.getY(), end.getY(), factor), interpolate(start.getZ(), end.getZ(), factor));
    }
}
