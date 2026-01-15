package ru.vsu.cs.cg.vertexremover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exceptions.VertexRemoverException;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.utils.MessageConstants;
import ru.vsu.cs.cg.utils.ModelUtils;
import ru.vsu.cs.cg.utils.RemovalUtils;
import ru.vsu.cs.cg.vertexremover.dto.VertexRemovalResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Реализация интерфейса для удаления вершин из 3D моделей.
 * Этот класс предоставляет функционал для удаления вершин из модели
 * с автоматическим удалением всех полигонов, которые используют удаляемые вершины.
 */
public final class VertexRemoverImpl implements VertexRemover {
    private static final Logger log = LoggerFactory.getLogger(VertexRemoverImpl.class);

    /**
     * Удаляет указанные вершины из модели.
     * При удалении вершины также удаляются все полигоны, которые к ней прикреплены.
     *
     * @param model         Исходная модель
     * @param vertexIndices Набор индексов вершин для удаления (0-based)
     * @param clearUnused   Флаг очистки неиспользуемых элементов
     * @return Результат операции удаления
     * @throws VertexRemoverException Если возникла ошибка при удалении
     */
    @Override
    public VertexRemovalResult removeVertices(Model model, Set<Integer> vertexIndices,
                                              boolean clearUnused)
            throws VertexRemoverException {
        log.info("VERTEX_REMOVAL_SERVICE_REMOVE_VERTICES_START: " +
                        "начало удаления вершин, количество вершин для удаления: {}, " +
                        "всего вершин в модели: {}, всего полигонов: {}",
                vertexIndices.size(), model.getVertices().size(), model.getPolygons().size());

        validateRemovalRequest(model, vertexIndices);

        Set<Integer> polygonsToRemove = ModelUtils.findPolygonsContainingVertices(model, vertexIndices);
        log.info("VERTEX_REMOVAL_SERVICE_FIND_POLYGONS: " +
                        "найдено полигонов для удаления: {}, индексы: {}",
                polygonsToRemove.size(), polygonsToRemove);

        Set<Integer> allVerticesToRemove = new HashSet<>(vertexIndices);
        Set<Integer> textureIndicesToRemove = new HashSet<>();
        Set<Integer> normalIndicesToRemove = new HashSet<>();

        if (clearUnused) {
            processUnusedElements(model, polygonsToRemove, allVerticesToRemove,
                    textureIndicesToRemove, normalIndicesToRemove);
        } else {
            ModelUtils.removePolygonsFromModel(model, polygonsToRemove);
        }

        log.info("VERTEX_REMOVAL_SERVICE_REMOVE_POLYGONS_FROM_MODEL: " +
                "полигоны удалены из модели, осталось полигонов: {}", model.getPolygons().size());

        ModelUtils.removeVerticesFromModel(model, allVerticesToRemove);
        log.info("VERTEX_REMOVAL_SERVICE_REMOVE_VERTICES_FROM_MODEL: " +
                "вершины удалены из модели, осталось вершин: {}", model.getVertices().size());

        performCleanupAndReindex(model, allVerticesToRemove, textureIndicesToRemove,
                normalIndicesToRemove, clearUnused);

        log.info("VERTEX_REMOVAL_SERVICE_REINDEX_MODEL_COMPLETE: " +
                        "модель переиндексирована, финальное количество вершин: {}, " +
                        "финальное количество полигонов: {}",
                model.getVertices().size(), model.getPolygons().size());

        return new VertexRemovalResult(
                allVerticesToRemove.size(),
                polygonsToRemove.size(),
                polygonsToRemove
        );
    }

    /**
     * Проверяет, можно ли удалить указанные вершины.
     *
     * @param model         Модель для проверки
     * @param vertexIndices Набор индексов вершин для проверки
     * @return true если вершины можно удалить, false в противном случае
     */
    private boolean canRemoveVertices(Model model, Set<Integer> vertexIndices) {
        log.info("VERTEX_REMOVAL_SERVICE_CAN_REMOVE_VERTICES_CHECK: " +
                        "проверка возможности удаления вершин, количество: {}, " +
                        "максимальный индекс в модели: {}",
                vertexIndices == null ? 0 : vertexIndices.size(),
                model.getVertices().size() - 1);

        if (vertexIndices == null || vertexIndices.isEmpty()) {
            return vertexIndices != null;
        }

        for (Integer index : vertexIndices) {
            if (!ModelUtils.isValidVertexIndex(model, index)) {
                log.warn("VERTEX_REMOVAL_SERVICE_CAN_REMOVE_VERTICES_INVALID_INDEX: " +
                                "неверный индекс вершины: {}, допустимый диапазон: 0-{}",
                        index, model.getVertices().size() - 1);
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateRemovalRequest(Model model, Set<Integer> vertexIndices)
            throws VertexRemoverException {
        if (!canRemoveVertices(model, vertexIndices)) {
            throw new VertexRemoverException(MessageConstants.VERTICES_CANNOT_DELETE_FAILURE);
        }
    }

    /**
     * Обрабатывает неиспользуемые элементы. Сначала собирает данные из удаляемых полигонов,
     * потом удаляет полигоны, и затем определяет какие элементы стали неиспользуемыми.
     *
     * @param model                  Модель для обработки
     * @param polygonsToRemove       Полигоны для удаления
     * @param allVerticesToRemove    Все вершины для удаления
     * @param textureIndicesToRemove Индексы текстур для удаления
     * @param normalIndicesToRemove  Индексы нормалей для удаления
     */
    private void processUnusedElements(Model model,
                                       Set<Integer> polygonsToRemove,
                                       Set<Integer> allVerticesToRemove,
                                       Set<Integer> textureIndicesToRemove,
                                       Set<Integer> normalIndicesToRemove) {

        Set<Integer> verticesInRemovedPolygons = ModelUtils.collectVerticesFromPolygons(model, polygonsToRemove);
        Set<Integer> textureIndicesInRemovedPolygons = ModelUtils.collectTextureIndicesFromPolygons(model, polygonsToRemove);
        Set<Integer> normalIndicesInRemovedPolygons = ModelUtils.collectNormalIndicesFromPolygons(model, polygonsToRemove);

        ModelUtils.removePolygonsFromModel(model, polygonsToRemove);

        Set<Integer> usedVertices = ModelUtils.collectUsedVertices(model);
        Set<Integer> usedTextureIndices = ModelUtils.collectUsedTextureIndices(model);
        Set<Integer> usedNormalIndices = ModelUtils.collectUsedNormalIndices(model);

        addUnusedElements(allVerticesToRemove, verticesInRemovedPolygons, usedVertices);
        addUnusedElements(textureIndicesToRemove, textureIndicesInRemovedPolygons, usedTextureIndices);
        addUnusedElements(normalIndicesToRemove, normalIndicesInRemovedPolygons, usedNormalIndices);
    }

    /**
     * Добавляет неиспользуемые элементы в набор для удаления.
     *
     * @param elementsToRemove            Набор элементов для удаления
     * @param elementsFromRemovedPolygons Элементы из удаленных полигонов
     * @param usedElements                Используемые элементы
     */
    private void addUnusedElements(Set<Integer> elementsToRemove,
                                   Set<Integer> elementsFromRemovedPolygons,
                                   Set<Integer> usedElements) {

        for (Integer element : elementsFromRemovedPolygons) {
            if (!usedElements.contains(element)) {
                elementsToRemove.add(element);
            }
        }
    }

    /**
     * Выполняет очистку и переиндексацию модели.
     *
     * @param model                  Модель для обработки
     * @param allVerticesToRemove    Все вершины для удаления
     * @param textureIndicesToRemove Индексы текстур для удаления
     * @param normalIndicesToRemove  Индексы нормалей для удаления
     * @param clearUnused            Флаг очистки неиспользуемых элементов
     */
    private void performCleanupAndReindex(Model model,
                                          Set<Integer> allVerticesToRemove,
                                          Set<Integer> textureIndicesToRemove,
                                          Set<Integer> normalIndicesToRemove,
                                          boolean clearUnused) {

        if (clearUnused) {
            if (!textureIndicesToRemove.isEmpty()) {
                RemovalUtils.removeUnusedTextureVertices(model, textureIndicesToRemove);
            }
            if (!normalIndicesToRemove.isEmpty()) {
                RemovalUtils.removeUnusedNormals(model, normalIndicesToRemove);
            }
        }

        if (clearUnused) {
            ModelUtils.partialReindexModel(model, allVerticesToRemove,
                    textureIndicesToRemove, normalIndicesToRemove);
        } else {
            ModelUtils.fullReindexModel(model, allVerticesToRemove,
                    textureIndicesToRemove, normalIndicesToRemove);
        }
    }
}
