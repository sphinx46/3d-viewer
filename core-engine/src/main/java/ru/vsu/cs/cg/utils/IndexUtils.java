package ru.vsu.cs.cg.utils;

import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.Polygon;
import ru.vsu.cs.cg.utils.functional.PolygonDataExtractor;

import java.util.*;

/**
 * Утилитарный класс для работы с индексами в 3D модели.
 * Содержит методы для создания маппингов индексов и получения используемых индексов.
 */
public final class IndexUtils {

    public static final PolygonDataExtractor VERTEX_EXTRACTOR = Polygon::getVertexIndices;
    public static final PolygonDataExtractor TEXTURE_EXTRACTOR = Polygon::getTextureVertexIndices;
    public static final PolygonDataExtractor NORMAL_EXTRACTOR = Polygon::getNormalIndices;

    /**
     * Получает все индексы определенного типа, используемые в полигонах модели.
     *
     * @param model Модель для анализа
     * @param extractor Извлекатель данных для получения индексов определенного типа
     * @return Набор всех используемых индексов указанного типа
     */
    public static Set<Integer> getAllUsedIndices(Model model,
                                                 PolygonDataExtractor extractor) {
        Set<Integer> usedIndices = new HashSet<>();

        for (Polygon polygon : model.getPolygons()) {
            if (polygon != null) {
                usedIndices.addAll(extractor.extract(polygon));
            }
        }

        return usedIndices;
    }

    /**
     * Получает все индексы вершин, используемые в полигонах модели.
     *
     * @param model Модель для анализа
     * @return Набор всех используемых индексов вершин
     */
    public static Set<Integer> getAllUsedVertexIndices(Model model) {
        return getAllUsedIndices(model, VERTEX_EXTRACTOR);
    }

    /**
     * Получает все индексы текстурных координат, используемые в полигонах модели.
     *
     * @param model Модель для анализа
     * @return Набор всех используемых индексов текстурных координат
     */
    public static Set<Integer> getAllUsedTextureVertexIndices(Model model) {
        return getAllUsedIndices(model, TEXTURE_EXTRACTOR);
    }

    /**
     * Получает все индексы нормалей, используемые в полигонах модели.
     *
     * @param model Модель для анализа
     * @return Набор всех используемых индексов нормалей
     */
    public static Set<Integer> getAllUsedNormalIndices(Model model) {
        return getAllUsedIndices(model, NORMAL_EXTRACTOR);
    }


    /**
     * Создает отображение старых индексов на новые с учетом удаленных индексов.
     *
     * @param usedIndices Набор используемых индексов
     * @param removedIndices Набор удаляемых индексов
     * @return Отображение старых индексов на новые
     */
    public static Map<Integer, Integer> createIndexMappingExcluding(Set<Integer> usedIndices,
                                                                    Set<Integer> removedIndices) {
        if (usedIndices == null || usedIndices.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> sortedUsedIndices = new ArrayList<>(usedIndices);
        Collections.sort(sortedUsedIndices);

        Map<Integer, Integer> mapping = new HashMap<>();
        int newIndex = 0;

        for (Integer oldIndex : sortedUsedIndices) {
            if (removedIndices == null || !removedIndices.contains(oldIndex)) {
                mapping.put(oldIndex, newIndex++);
            }
        }

        return mapping;
    }

    /**
     * Создает отображение старых индексов на новые для ВСЕХ элементов, кроме удаленных.
     * Создает маппинг для всех индексов от 0 до максимального используемого индекса.
     *
     * @param usedIndices Набор используемых индексов
     * @param removedIndices Набор удаляемых индексов
     * @return Отображение старых индексов на новые
     */
    public static Map<Integer, Integer> createFullIndexMapping(Set<Integer> usedIndices,
                                                               Set<Integer> removedIndices) {
        if (usedIndices == null || usedIndices.isEmpty()) {
            return Collections.emptyMap();
        }

        int maxUsedIndex = usedIndices.stream()
                .max(Integer::compareTo)
                .orElse(-1);

        if (maxUsedIndex < 0) {
            return Collections.emptyMap();
        }

        List<Integer> sortedUsedIndices = new ArrayList<>(usedIndices);
        Collections.sort(sortedUsedIndices);

        Map<Integer, Integer> mapping = new HashMap<>();
        int newIndex = 0;
        int currentIndex = 0;

        for (int oldIndex = 0; oldIndex <= maxUsedIndex; oldIndex++) {
            if (currentIndex < sortedUsedIndices.size()
                    && sortedUsedIndices.get(currentIndex) == oldIndex) {
                currentIndex++;
                if (removedIndices != null && removedIndices.contains(oldIndex)) {
                    continue;
                }
                mapping.put(oldIndex, newIndex++);
            }
        }

        return mapping;
    }
}
