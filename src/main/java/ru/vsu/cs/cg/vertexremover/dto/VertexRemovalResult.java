package ru.vsu.cs.cg.vertexremover.dto;

import java.util.Set;

/**
 * Результат операции удаления вершин.
 */
public final class VertexRemovalResult {
    private final int removedVerticesCount;
    private final int removedPolygonsCount;
    private final Set<Integer> affectedPolygonIndices;

    /**
     * Конструктор результата удаления вершин.
     *
     * @param removedVerticesCount Количество удаленных вершин
     * @param removedPolygonsCount Количество удаленных полигонов
     * @param affectedPolygonIndices Индексы затронутых полигонов
     */
    public VertexRemovalResult(int removedVerticesCount,
                               int removedPolygonsCount,
                               Set<Integer> affectedPolygonIndices) {
        this.removedVerticesCount = removedVerticesCount;
        this.removedPolygonsCount = removedPolygonsCount;
        this.affectedPolygonIndices = affectedPolygonIndices;
    }

    /**
     * Возвращает количество удаленных вершин.
     *
     * @return Количество удаленных вершин
     */
    public int getRemovedVerticesCount() {
        return removedVerticesCount;
    }

    /**
     * Возвращает количество удаленных полигонов.
     *
     * @return Количество удаленных полигонов
     */
    public int getRemovedPolygonsCount() {
        return removedPolygonsCount;
    }
}