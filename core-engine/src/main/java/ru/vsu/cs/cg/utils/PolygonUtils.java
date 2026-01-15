package ru.vsu.cs.cg.utils;

import ru.vsu.cs.cg.model.Polygon;

import java.util.*;

/**
 * Утилитарный класс для работы с полигонами 3D модели.
 * Содержит методы для проверки и переиндексации полигонов.
 */
public final class PolygonUtils {

    /**
     * Проверяет, содержит ли полигон хотя бы одну из указанных вершин.
     *
     * @param polygon Полигон для проверки
     * @param vertexIndices Набор индексов вершин для проверки
     * @return true если полигон содержит хотя бы одну из указанных вершин
     */
    public static boolean polygonContainsAnyVertex(Polygon polygon, Set<Integer> vertexIndices) {
        if (polygon == null || vertexIndices == null) {
            return false;
        }

        for (Integer vertexIndex : polygon.getVertexIndices()) {
            if (vertexIndices.contains(vertexIndex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Переиндексирует полигон с использованием отображения индексов.
     *
     * @param polygon Полигон для переиндексации
     * @param vertexIndexMapping Отображение индексов вершин
     * @param textureIndexMapping Отображение индексов текстурных координат
     * @param normalIndexMapping Отображение индексов нормалей
     * @return Переиндексированный полигон или null если полигон нужно удалить
     */
    public static Polygon reindexPolygon(Polygon polygon,
                                         Map<Integer, Integer> vertexIndexMapping,
                                         Map<Integer, Integer> textureIndexMapping,
                                         Map<Integer, Integer> normalIndexMapping) {
        if (polygon == null) {
            return null;
        }

        List<Integer> newVertexIndices = reindexIndices(polygon.getVertexIndices(), vertexIndexMapping);
        if (newVertexIndices == null || newVertexIndices.size() < 3) {
            return null;
        }

        Polygon newPolygon = new Polygon();
        newPolygon.setVertexIndices(new ArrayList<>(newVertexIndices));

        if (!polygon.getTextureVertexIndices().isEmpty()) {
            List<Integer> newTextureIndices = reindexIndices(polygon.getTextureVertexIndices(),
                    textureIndexMapping);
            if (newTextureIndices == null) {
                return null;
            }
            newPolygon.setTextureVertexIndices(new ArrayList<>(newTextureIndices));
        }

        if (!polygon.getNormalIndices().isEmpty()) {
            List<Integer> newNormalIndices = reindexIndices(polygon.getNormalIndices(), normalIndexMapping);
            if (newNormalIndices == null) {
                return null;
            }
            newPolygon.setNormalIndices(new ArrayList<>(newNormalIndices));
        }

        return newPolygon;
    }

    /**
     * Переиндексирует список индексов с использованием маппинга.
     *
     * @param oldIndices Старые индексы
     * @param indexMapping Маппинг индексов
     * @return Новые индексы или null если какой-то индекс не найден
     */
    private static List<Integer> reindexIndices(List<Integer> oldIndices,
                                                Map<Integer, Integer> indexMapping) {
        List<Integer> newIndices = new ArrayList<>();

        for (Integer oldIndex : oldIndices) {
            Integer newIndex = indexMapping.get(oldIndex);
            if (newIndex == null) {
                return null;
            }
            newIndices.add(newIndex);
        }

        return newIndices;
    }
}