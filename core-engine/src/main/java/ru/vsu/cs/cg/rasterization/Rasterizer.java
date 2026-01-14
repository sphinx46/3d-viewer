package ru.vsu.cs.cg.rasterization;

import ru.vsu.cs.cg.math.Vector2f;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.PixelWriter;
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
                        pixelNormal = pixelNormal.normalized();
                        float lightIntensity = Math.max(0, pixelNormal.dot(lightDirection));
                        finalPixelColor = applyLight(finalPixelColor, lightIntensity);
                    }

                    pixelWriter.setPixel(x, y, finalPixelColor);
                }
            }
        }
    }

    private void drawLine(PixelWriter pixelWriter, int width, int height, Vector3f start, Vector3f end, Color color) {
        float x1 = start.getX();
        float y1 = start.getY();
        float x2 = end.getX();
        float y2 = end.getY();

        if ((x1 < 0 && x2 < 0) || (x1 > width && x2 > width) ||
                (y1 < 0 && y2 < 0) || (y1 > height && y2 > height)) {
            return;
        }

        float dx = x2 - x1;
        float dy = y2 - y1;
        float steps = Math.max(Math.abs(dx), Math.abs(dy));

        if (steps > (width + height) * 3) return;

        if (steps == 0) {
            int px = Math.round(x1);
            int py = Math.round(y1);
            if (px >= 0 && px < width && py >= 0 && py < height) {
                if (zBuffer.checkAndSet(px, py, 1.0f / Math.max(start.getZ(), EPSILON))) {
                    pixelWriter.setPixel(px, py, color);
                }
            }
            return;
        }

        float xIncrement = dx / steps;
        float yIncrement = dy / steps;

        float zStart = Math.max(start.getZ(), EPSILON);
        float zEnd = Math.max(end.getZ(), EPSILON);
        float invZStart = 1.0f / zStart;
        float invZEnd = 1.0f / zEnd;

        float x = x1;
        float y = y1;

        for (int i = 0; i <= steps; i++) {
            int pixelX = Math.round(x);
            int pixelY = Math.round(y);

            if (pixelX >= 0 && pixelX < width && pixelY >= 0 && pixelY < height) {
                float t = (float) i / steps;
                float currentInvZ = interpolate(invZStart, invZEnd, t);
                float currentZ = 1.0f / currentInvZ;

                float biasFactor = 0.0005f;
                float biasAbsolute = 0.00002f;
                float biasedZ = currentZ - (currentZ * biasFactor + biasAbsolute);

                if (zBuffer.checkAndSet(pixelX, pixelY, biasedZ)) {
                    pixelWriter.setPixel(pixelX, pixelY, color);
                }
            }

            x += xIncrement;
            y += yIncrement;
        }
    }

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
        return new Vector2f(interpolate(start.getX(), end.getX(), factor), interpolate(start.getY(), end.getY(), factor));
    }

    private Vector3f interpolate(Vector3f start, Vector3f end, float factor) {
        return new Vector3f(interpolate(start.getX(), end.getX(), factor), interpolate(start.getY(), end.getY(), factor), interpolate(start.getZ(), end.getZ(), factor));
    }
}