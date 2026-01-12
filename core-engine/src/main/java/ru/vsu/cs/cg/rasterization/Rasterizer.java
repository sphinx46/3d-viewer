package ru.vsu.cs.cg.rasterization;

import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.render_engine.PixelWriter;
import javafx.scene.paint.Color;

/**
 * Класс растеризатора для преобразования 3D треугольников в 2D пиксели на экране
 */
public class Rasterizer {
    private final ZBuffer zBuffer;

    private static final float EPSILON = 1e-5f;

    public Rasterizer(ZBuffer zBuffer) {
        this.zBuffer = zBuffer;
    }

    /**
     * Отрисовывает треугольник.
     * Сортирует вершины по высоте и вызывает метод отрисовки сканирующих линий.
     *
     * @param pixelWriter интерфейс для записи пикселя на экран
     * @param vertex1 координаты 1 вершины треугольника в мировом пространстве
     * @param vertex2 координаты 2 вершины треугольника в мировом пространстве
     * @param vertex3 координаты 3 вершины треугольника в мировом пространстве
     * @param uv1 текстурные координаты для 1 вершины
     * @param uv2 текстурные координаты для 2 вершины
     * @param uv3 текстурные координаты для 3 вершины
     * @param normal1 нормаль в 1 вершине
     * @param normal2 нормаль в 1 вершине
     * @param normal3 нормаль в 1 вершине
     * @param texture текстура для наложения
     * @param lightDirection направление источника света
     * @param settings настройки для рендера (включение/выключение текстур, освещения)
     */
    public void drawTriangle(
            PixelWriter pixelWriter,
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
                pixelWriter,
                vertex1, vertex2,
                vertex1, vertex3,
                uv1, uv2,
                uv1, uv3,
                normal1, normal2,
                normal1, normal3,
                texture, lightDirection, settings
        );


        drawScanlinePart(
                pixelWriter,
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

            drawLine(pixelWriter, vertex1, vertex2, gridColor);
            drawLine(pixelWriter, vertex2, vertex3, gridColor);
            drawLine(pixelWriter, vertex1, vertex3, gridColor);
        }
    }


    /**
     * Рисует горизонтальные полосы (scanlines) для части треугольника.
     * Использует перспективно-корректную интерполяцию.
     *
     * @param pixelWriter интерфейс для отрисовки пикселя на экран
     * @param edgeStartA начальная точка для ребра А
     * @param edgeEndA конечная точка для ребра А
     * @param edgeStartB начальная точка для ребра В
     * @param edgeEndB конечная точка для ребра В
     * @param uvStartA Текстурные координаты в начале ребра А
     * @param uvEndA Текстурные координаты в конце ребра А
     * @param uvStartB Текстурные координаты в начале ребра В
     * @param uvEndB Текстурные координаты в конце ребра В
     * @param normalStartA Нормаль в начале ребра А
     * @param normalEndA Нормаль в конце ребра А
     * @param normalStartB Нормаль в начале ребра В
     * @param normalEndB Нормаль в конце ребра В
     * @param texture текстура для наложения
     * @param lightDirection вектор направления источника света
     * @param settings настройки для рендера (включение/выключение текстур, освещения)
     */
    private void drawScanlinePart(
            PixelWriter pixelWriter,
            Vector3f edgeStartA, Vector3f edgeEndA,
            Vector3f edgeStartB, Vector3f edgeEndB,
            Vector2f uvStartA, Vector2f uvEndA,
            Vector2f uvStartB, Vector2f uvEndB,
            Vector3f normalStartA, Vector3f normalEndA,
            Vector3f normalStartB, Vector3f normalEndB,
            Texture texture, Vector3f lightDirection, RasterizerSettings settings
    ) {

        int scanlineYStart = (int) Math.ceil(Math.max(edgeStartA.getY(), edgeStartB.getY()));
        int scanlineYEnd = (int) Math.ceil(Math.min(edgeEndA.getY(), edgeEndB.getY()));

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

            if (settings.isUseTexture() && uvStartA != null && uvEndA != null && uvStartB != null && uvEndB != null) {
                uvOverDepthStart = interpolate(uvStartA.multiply(invDepthStartA), uvEndA.multiply(invDepthEndA), verticalFactorA);
                uvOverDepthEnd   = interpolate(uvStartB.multiply(invDepthStartB), uvEndB.multiply(invDepthEndB), verticalFactorB);
            }

            if (settings.isUseLighting() && normalStartA != null && normalEndA != null && normalStartB != null && normalEndB != null) {
                normalOverDepthStart = interpolate(normalStartA.multiply(invDepthStartA), normalEndA.multiply(invDepthEndA), verticalFactorA);
                normalOverDepthEnd   = interpolate(normalStartB.multiply(invDepthStartB), normalEndB.multiply(invDepthEndB), verticalFactorB);
            }
            if (scanlineStartX > scanlineEndX) {
                float tempX = scanlineStartX; scanlineStartX = scanlineEndX; scanlineEndX = tempX;

                float tempInvDepth = scanlineStartInvDepth; scanlineStartInvDepth = scanlineEndInvDepth; scanlineEndInvDepth = tempInvDepth;

                Vector2f tempUV = uvOverDepthStart; uvOverDepthStart = uvOverDepthEnd; uvOverDepthEnd = tempUV;

                Vector3f tempNormal = normalOverDepthStart; normalOverDepthStart = normalOverDepthEnd; normalOverDepthEnd = tempNormal;
            }

            int pixelXStart = (int) Math.ceil(scanlineStartX);
            int pixelXEnd   = (int) Math.ceil(scanlineEndX);

            float scanlineWidth = scanlineEndX - scanlineStartX;
            if (scanlineWidth == 0) continue;
            float invScanlineWidth = 1.0f / scanlineWidth;

            for (int x = pixelXStart; x < pixelXEnd; x++) {
                float horizontalFactor = (x - scanlineStartX) * invScanlineWidth;

                float currentInvDepth = interpolate(scanlineStartInvDepth, scanlineEndInvDepth, horizontalFactor);

                float currentPixelDepth = 1.0f / currentInvDepth;

                if (zBuffer.checkAndSet(x, y, currentPixelDepth)) {

                    Color finalPixelColor = settings.getDefaultColor();

                    if (settings.isUseTexture() && texture != null && uvOverDepthStart != null && uvOverDepthEnd != null) {
                        Vector2f interpolatedUVOverDepth = interpolate(uvOverDepthStart, uvOverDepthEnd, horizontalFactor);

                        Vector2f finalUV = interpolatedUVOverDepth.multiply(currentPixelDepth);

                        finalPixelColor = texture.getPixel(finalUV.getX(), finalUV.getY());
                    }

                    if (settings.isUseLighting() && normalOverDepthEnd != null && normalOverDepthStart != null) {
                        Vector3f interpolatedNormalOverDepth = interpolate(normalOverDepthStart, normalOverDepthEnd, horizontalFactor);

                        Vector3f pixelNormal = interpolatedNormalOverDepth.multiply(currentPixelDepth);

                        pixelNormal = pixelNormal.normalized();

                        float lightIntensity = Math.max(0, pixelNormal.dot(lightDirection));

                        finalPixelColor = applyLight(finalPixelColor, lightIntensity);
                    }

                    pixelWriter.setPixel(x, y, finalPixelColor);
                }
            }
        }
    }

    // --- Вспомогательные методы ---

    /**
     * Рисует линию между двумя точками, используя алгоритм Брезенхема.
     * Учитывает Z-буфер для корректного перекрытия.
     * @param pixelWriter интерфейс для отрисовки пикселя на экран
     * @param start вектор начала линии
     * @param end вектор конца линии
     * @param color цвет линии
     */
    private void drawLine(PixelWriter pixelWriter, Vector3f start, Vector3f end, Color color) {
        int x0 = (int) start.getX();
        int y0 = (int) start.getY();
        int x1 = (int) end.getX();
        int y1 = (int) end.getY();

        float zStart = Math.max(start.getZ(), EPSILON);
        float zEnd = Math.max(end.getZ(), EPSILON);
        float invZStart = 1.0f / zStart;
        float invZEnd = 1.0f / zEnd;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int stepX = x0 < x1 ? 1 : -1;
        int stepY = y0 < y1 ? 1 : -1;
        int error = dx - dy;

        float totalDist = Math.max(dx, dy);
        if (totalDist == 0) return;

        int x = x0;
        int y = y0;

        float stepCount = 0;

        while (true) {
            float t = stepCount / totalDist;
            float currentInvZ = interpolate(invZStart, invZEnd, t);
            float currentZ = 1.0f / currentInvZ;

            float biasFactor = 0.003f;
            float offset = currentZ * biasFactor;

            float biasedZ = currentZ - offset;

            if (zBuffer.checkAndSet(x, y, biasedZ)) {
                pixelWriter.setPixel(x, y, color);
            }

            if (x == x1 && y == y1) break;

            int e2 = 2 * error;
            if (e2 > -dy) {
                error -= dy;
                x += stepX;
            }
            if (e2 < dx) {
                error += dx;
                y += stepY;
            }

            stepCount++;
        }
    }

    /**
     * Применение освещения с учетом добавления ambientLight
     * что бы не было сильно темных участков
     *
     * @param baseColor Цвет для освещения
     * @param intensity Интенсивность цвета
     * @return Освещенный/затемненный цвет
     */

    private Color applyLight(Color baseColor, float intensity) {
        float ambientLight = 0.3f;
        double totalFactor = Math.min(1.0, ambientLight + intensity);

        return Color.color(
                Math.min(1.0, baseColor.getRed() * totalFactor),
                Math.min(1.0, baseColor.getGreen() * totalFactor),
                Math.min(1.0, baseColor.getBlue() * totalFactor)
        );
    }

    private float interpolate(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

    private Vector2f interpolate(Vector2f start, Vector2f end, float factor) {
        return new Vector2f(
                interpolate(start.getX(), end.getX(), factor),
                interpolate(start.getY(), end.getY(), factor)
        );
    }

    private Vector3f interpolate(Vector3f start, Vector3f end, float factor) {
        return new Vector3f(
                interpolate(start.getX(), end.getX(), factor),
                interpolate(start.getY(), end.getY(), factor),
                interpolate(start.getZ(), end.getZ(), factor)
        );
    }
}