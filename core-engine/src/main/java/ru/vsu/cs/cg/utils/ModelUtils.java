package ru.vsu.cs.cg.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.utils.functional.PolygonDataExtractor;

import java.util.*;

/**
 * Утилитарный класс для операций с 3D моделью.
 * Содержит основные методы для работы с вершинами и полигонами модели.
 */
public final class ModelUtils {
    private static final Logger log = LoggerFactory.getLogger(ModelUtils.class);

    public static final PolygonDataExtractor VERTICES_EXTRACTOR = Polygon::getVertexIndices;
    public static final PolygonDataExtractor TEXTURE_INDICES_EXTRACTOR = Polygon::getTextureVertexIndices;
    public static final PolygonDataExtractor NORMAL_INDICES_EXTRACTOR = Polygon::getNormalIndices;

    /**
     * Находит все полигоны, которые содержат указанные вершины.
     *
     * @param model Модель для поиска
     * @param vertexIndices Набор индексов вершин для поиска
     * @return Набор индексов полигонов, содержащих указанные вершины
     */
    public static Set<Integer> findPolygonsContainingVertices(Model model, Set<Integer> vertexIndices) {
        log.info("VERTEX_REMOVAL_SERVICE_FIND_POLYGONS_CONTAINING_VERTICES_START: " +
                "поиск полигонов, содержащих указанные вершины");

        Set<Integer> polygonsToRemove = new HashSet<>();

        for (int polygonIndex = 0; polygonIndex < model.getPolygons().size(); polygonIndex++) {
            Polygon polygon = model.getPolygons().get(polygonIndex);
            boolean contains = PolygonUtils.polygonContainsAnyVertex(polygon, vertexIndices);
            if (contains) {
                log.debug("VERTEX_REMOVAL_SERVICE_POLYGON_CONTAINS_VERTICES: " +
                                "полигон {} содержит вершины: {}, вершины полигона: {}",
                        polygonIndex, vertexIndices, polygon.getVertexIndices());
                polygonsToRemove.add(polygonIndex);
            }
        }

        log.info("VERTEX_REMOVAL_SERVICE_FIND_POLYGONS_CONTAINING_VERTICES_COMPLETE: " +
                "найдено полигонов: {}", polygonsToRemove.size());

        return polygonsToRemove;
    }

    /**
     * Собирает данные из указанных полигонов с использованием экстрактора.
     *
     * @param model Модель для анализа
     * @param polygonIndices Индексы полигонов
     * @param extractor Экстрактор данных из полигона
     * @return Набор данных из указанных полигонов
     */
    private static Set<Integer> collectDataFromPolygons(Model model,
                                                        Set<Integer> polygonIndices,
                                                        PolygonDataExtractor extractor) {
        Set<Integer> data = new HashSet<>();

        for (Integer polygonIndex : polygonIndices) {
            if (polygonIndex >= 0 && polygonIndex < model.getPolygons().size()) {
                Polygon polygon = model.getPolygons().get(polygonIndex);
                data.addAll(extractor.extract(polygon));
            } else {
                log.warn("MODEL_UTILS_INVALID_POLYGON_INDEX: " +
                        "некорректный индекс полигона: {}", polygonIndex);
            }
        }

        return data;
    }

    /**
     * Собирает вершины из указанных полигонов.
     *
     * @param model Модель для анализа
     * @param polygonIndices Индексы полигонов
     * @return Набор вершин из указанных полигонов
     */
    public static Set<Integer> collectVerticesFromPolygons(Model model, Set<Integer> polygonIndices) {
        return collectDataFromPolygons(model, polygonIndices, VERTICES_EXTRACTOR);
    }

    /**
     * Собирает индексы текстурных координат из указанных полигонов.
     *
     * @param model Модель для анализа
     * @param polygonIndices Индексы полигонов
     * @return Набор индексов текстурных координат из указанных полигонов
     */
    public static Set<Integer> collectTextureIndicesFromPolygons(Model model, Set<Integer> polygonIndices) {
        return collectDataFromPolygons(model, polygonIndices, TEXTURE_INDICES_EXTRACTOR);
    }

    /**
     * Собирает индексы нормалей из указанных полигонов.
     *
     * @param model Модель для анализа
     * @param polygonIndices Индексы полигонов
     * @return Набор индексов нормалей из указанных полигонов
     */
    public static Set<Integer> collectNormalIndicesFromPolygons(Model model,
                                                                Set<Integer> polygonIndices) {
        return collectDataFromPolygons(model, polygonIndices, NORMAL_INDICES_EXTRACTOR);
    }

    /**
     * Собирает используемые данные из модели с использованием экстрактора.
     *
     * @param model Модель для анализа
     * @param extractor Экстрактор данных из полигона
     * @return Набор используемых данных
     */
    public static Set<Integer> collectUsedData(Model model, PolygonDataExtractor extractor) {
        Set<Integer> usedData = new HashSet<>();

        for (Polygon polygon : model.getPolygons()) {
            usedData.addAll(extractor.extract(polygon));
        }

        return usedData;
    }

    /**
     * Собирает используемые вершины из модели.
     *
     * @param model Модель для анализа
     * @return Набор используемых вершин
     */
    public static Set<Integer> collectUsedVertices(Model model) {
        return collectUsedData(model, VERTICES_EXTRACTOR);
    }

    /**
     * Собирает используемые индексы текстурных координат из модели.
     *
     * @param model Модель для анализа
     * @return Набор используемых индексов текстурных координат
     */
    public static Set<Integer> collectUsedTextureIndices(Model model) {
        return collectUsedData(model, TEXTURE_INDICES_EXTRACTOR);
    }

    /**
     * Собирает используемые индексы нормалей из модели.
     *
     * @param model Модель для анализа
     * @return Набор используемых индексов нормалей
     */
    public static Set<Integer> collectUsedNormalIndices(Model model) {
        return collectUsedData(model, NORMAL_INDICES_EXTRACTOR);
    }

    /**
     * Удаляет указанные вершины из массивов модели.
     *
     * @param model Модель для удаления вершин
     * @param vertexIndices Набор индексов вершин для удаления
     */
    public static void removeVerticesFromModel(Model model, Set<Integer> vertexIndices) {
        log.info("MODEL_UTILS_REMOVE_VERTICES_START: " +
                "удаление вершин из модели, количество: {}", vertexIndices.size());

        RemovalUtils.removeVerticesFromModel(model, vertexIndices);

        log.info("MODEL_UTILS_REMOVE_VERTICES_COMPLETE: " +
                "вершины удалены, осталось вершин: {}", model.getVertices().size());
    }

    /**
     * Удаляет указанные полигоны из модели.
     *
     * @param model Модель для удаления полигонов
     * @param polygonIndices Набор индексов полигонов для удаления
     */
    public static void removePolygonsFromModel(Model model, Set<Integer> polygonIndices) {
        log.info("MODEL_UTILS_REMOVE_POLYGONS_START: " +
                "удаление полигонов из модели, количество: {}", polygonIndices.size());

        RemovalUtils.removePolygonsFromModel(model, polygonIndices);

        log.info("MODEL_UTILS_REMOVE_POLYGONS_COMPLETE: " +
                "полигоны удалены, осталось полигонов: {}", model.getPolygons().size());
    }

    /**
     * Проверяет, является ли индекс вершины допустимым для модели.
     *
     * @param model Модель для проверки
     * @param vertexIndex Индекс вершины для проверки
     * @return true если индекс допустим, false в противном случае
     */
    public static boolean isValidVertexIndex(Model model, int vertexIndex) {
        return vertexIndex >= 0 && vertexIndex < model.getVertices().size();
    }

    /**
     * Частичная переиндексация модели только для используемых элементов.
     * Используется при clearUnused = true.
     *
     * @param model Модель для переиндексации
     * @param removedVertexIndices Удаляемые индексы вершин
     * @param removedTextureIndices Удаляемые индексы текстурных координат
     * @param removedNormalIndices Удаляемые индексы нормалей
     */
    public static void partialReindexModel(Model model,
                                           Set<Integer> removedVertexIndices,
                                           Set<Integer> removedTextureIndices,
                                           Set<Integer> removedNormalIndices) {
        log.info("MODEL_UTILS_PARTIAL_REINDEX_START: " +
                "частичная переиндексация модели");

        if (model.getPolygons().isEmpty()) {
            log.info("MODEL_UTILS_PARTIAL_REINDEX_SKIP: " +
                    "модель не содержит полигонов, переиндексация не требуется");
            return;
        }

        Set<Integer> usedVertexIndices = IndexUtils.getAllUsedVertexIndices(model);
        Set<Integer> usedTextureIndices = IndexUtils.getAllUsedTextureVertexIndices(model);
        Set<Integer> usedNormalIndices = IndexUtils.getAllUsedNormalIndices(model);

        Map<Integer, Integer> vertexIndexMapping =
                IndexUtils.createIndexMappingExcluding(usedVertexIndices, removedVertexIndices);
        Map<Integer, Integer> textureIndexMapping =
                IndexUtils.createIndexMappingExcluding(usedTextureIndices, removedTextureIndices);
        Map<Integer, Integer> normalIndexMapping =
                IndexUtils.createIndexMappingExcluding(usedNormalIndices, removedNormalIndices);

        List<Polygon> newPolygons = new ArrayList<>();

        for (Polygon polygon : model.getPolygons()) {
            Polygon reindexedPolygon = PolygonUtils.reindexPolygon(polygon,
                    vertexIndexMapping, textureIndexMapping, normalIndexMapping);
            if (reindexedPolygon != null) {
                newPolygons.add(reindexedPolygon);
            }
        }

        model.clearPolygons();
        model.addAllPolygons(newPolygons);

        log.info("MODEL_UTILS_PARTIAL_REINDEX_COMPLETE: " +
                "частичная переиндексация завершена");
    }

    /**
     * Полная переиндексация модели с сохранением всех оставшихся элементов.
     * Используется при clearUnused = false. Переиндексирует все индексы от 0 до максимального
     * используемого индекса, сохраняя порядок.
     *
     * @param model Модель для переиндексации
     * @param removedVertexIndices Удаляемые индексы вершин
     * @param removedTextureIndices Удаляемые индексы текстурных координат
     * @param removedNormalIndices Удаляемые индексы нормалей
     */
    public static void fullReindexModel(Model model,
                                        Set<Integer> removedVertexIndices,
                                        Set<Integer> removedTextureIndices,
                                        Set<Integer> removedNormalIndices) {
        log.info("MODEL_UTILS_FULL_REINDEX_START: " +
                "полная переиндексация модели");

        if (model.getPolygons().isEmpty()) {
            log.info("MODEL_UTILS_FULL_REINDEX_SKIP: " +
                    "модель не содержит полигонов, переиндексация не требуется");
            return;
        }

        Set<Integer> usedVertexIndices = IndexUtils.getAllUsedVertexIndices(model);
        Set<Integer> usedTextureIndices = IndexUtils.getAllUsedTextureVertexIndices(model);
        Set<Integer> usedNormalIndices = IndexUtils.getAllUsedNormalIndices(model);

        log.debug("MODEL_UTILS_FULL_REINDEX_USED_INDICES: " +
                        "используемые вершины: {}, текстуры: {}, нормали: {}",
                usedVertexIndices.size(), usedTextureIndices.size(), usedNormalIndices.size());

        Map<Integer, Integer> vertexIndexMapping =
                IndexUtils.createFullIndexMapping(usedVertexIndices, removedVertexIndices);
        Map<Integer, Integer> textureIndexMapping =
                IndexUtils.createFullIndexMapping(usedTextureIndices, removedTextureIndices);
        Map<Integer, Integer> normalIndexMapping =
                IndexUtils.createFullIndexMapping(usedNormalIndices, removedNormalIndices);

        log.debug("MODEL_UTILS_FULL_REINDEX_MAPPINGS: " +
                        "вершин: {}, текстур: {}, нормалей: {}",
                vertexIndexMapping.size(), textureIndexMapping.size(), normalIndexMapping.size());

        List<Polygon> newPolygons = new ArrayList<>();

        for (Polygon polygon : model.getPolygons()) {
            Polygon reindexedPolygon = PolygonUtils.reindexPolygon(polygon,
                    vertexIndexMapping, textureIndexMapping, normalIndexMapping);
            if (reindexedPolygon != null) {
                newPolygons.add(reindexedPolygon);
            }
        }

        model.clearPolygons();
        model.addAllPolygons(newPolygons);

        log.info("MODEL_UTILS_FULL_REINDEX_COMPLETE: " +
                "полная переиндексация завершена, " +
                "полигонов после переиндексации: {}", newPolygons.size());
    }
}
