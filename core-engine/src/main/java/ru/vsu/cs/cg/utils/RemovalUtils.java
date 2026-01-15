package ru.vsu.cs.cg.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.model.Model;

import java.util.*;

/**
 * Утилитарный класс для удаления элементов из 3D модели.
 * Содержит методы для безопасного удаления вершин, полигонов и других элементов.
 */
public final class RemovalUtils {
    private static final Logger log = LoggerFactory.getLogger(RemovalUtils.class);

    /**
     * Удаляет элементы из списка по указанным индексам.
     * Элементы удаляются в обратном порядке для сохранения корректности индексов.
     *
     * @param <T> Тип элементов в списке
     * @param list Список для удаления элементов
     * @param indices Набор индексов для удаления
     */
    public static <T> void removeElementsByIndices(List<T> list, Set<Integer> indices) {
        if (indices == null || indices.isEmpty()) {
            return;
        }

        List<Integer> sortedIndices = new ArrayList<>(indices);
        sortedIndices.sort(Collections.reverseOrder());

        for (Integer index : sortedIndices) {
            if (index >= 0 && index < list.size()) {
                list.remove(index.intValue());
            }
        }
    }

    /**
     * Удаляет указанные вершины из массивов модели.
     *
     * @param model Модель для удаления вершин
     * @param vertexIndices Набор индексов вершин для удаления
     */
    public static void removeVerticesFromModel(Model model, Set<Integer> vertexIndices) {
        log.info("VERTEX_REMOVAL_SERVICE_REMOVE_VERTICES_FROM_MODEL_START: " +
                        "удаление вершин из массивов модели, количество: {}",
                vertexIndices == null ? 0 : vertexIndices.size());

        removeElementsByIndices(model.getVerticesMutable(), vertexIndices);
    }

    /**
     * Удаляет указанные полигоны из модели.
     *
     * @param model Модель для удаления полигонов
     * @param polygonIndices Набор индексов полигонов для удаления
     */
    public static void removePolygonsFromModel(Model model, Set<Integer> polygonIndices) {
        log.info("VERTEX_REMOVAL_SERVICE_REMOVE_POLYGONS_FROM_MODEL_START: " +
                        "удаление полигонов из модели, количество: {}",
                polygonIndices == null ? 0 : polygonIndices.size());

        removeElementsByIndices(model.getPolygonsMutable(), polygonIndices);
    }

    /**
     * Удаляет неиспользуемые текстурные координаты из модели.
     *
     * @param model Модель для очистки
     * @param usedTextureIndices Набор используемых индексов текстурных координат
     */
    public static void removeUnusedTextureVertices(Model model, Set<Integer> usedTextureIndices) {
        log.info("REMOVAL_UTILS_REMOVE_UNUSED_TEXTURE_VERTICES_START: " +
                        "удаление неиспользуемых текстурных координат, используется: {}",
                usedTextureIndices == null ? 0 : usedTextureIndices.size());

        removeUnusedElements(
            model.getTextureVerticesMutable(),
                usedTextureIndices
        );
    }

    /**
     * Удаляет неиспользуемые нормали из модели.
     *
     * @param model Модель для очистки
     * @param usedNormalIndices Набор используемых индексов нормалей
     */
    public static void removeUnusedNormals(Model model, Set<Integer> usedNormalIndices) {
        log.info("REMOVAL_UTILS_REMOVE_UNUSED_NORMALS_START: " +
                        "удаление неиспользуемых нормалей, используется: {}",
                usedNormalIndices == null ? 0 : usedNormalIndices.size());

        removeUnusedElements(
            model.getNormalsMutable(),
                usedNormalIndices
        );
    }

    /**
     * Удаляет неиспользуемые элементы модели.
     *
     * @param <T> Тип элементов
     * @param sourceElements Исходные элементы
     * @param usedIndices Используемые индексы
     */
    private static <T> void removeUnusedElements(List<T> sourceElements,
                                                 Set<Integer> usedIndices) {

        if (sourceElements.isEmpty()) {
            return;
        }

        if (usedIndices == null || usedIndices.isEmpty()) {
            sourceElements.clear();
            return;
        }

        List<T> newElements = createNewElementsList(sourceElements, usedIndices);
        sourceElements.clear();
        sourceElements.addAll(newElements);
    }

    /**
     * Создает новый список элементов на основе используемых индексов.
     *
     * @param source Исходная коллекция
     * @param usedIndices Используемые индексы
     * @param <T> Тип элемента
     * @return Новая коллекция элементов
     */
    private static <T> List<T> createNewElementsList(List<T> source, Set<Integer> usedIndices) {
        List<Integer> sortedUsedIndices = new ArrayList<>(usedIndices);
        Collections.sort(sortedUsedIndices);

        List<T> newElements = new ArrayList<>();

        for (Integer oldIndex : sortedUsedIndices) {
            if (oldIndex >= 0 && oldIndex < source.size()) {
                newElements.add(source.get(oldIndex));
            }
        }

        return newElements;
    }
}
