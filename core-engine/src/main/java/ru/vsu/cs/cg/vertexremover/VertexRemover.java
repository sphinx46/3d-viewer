package ru.vsu.cs.cg.vertexremover;

import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.vertexremover.dto.VertexRemovalResult;
import ru.vsu.cs.cg.exceptions.VertexRemoverException;

import java.util.Set;

/**
 * Интерфейс для удаления вершин из 3D модели.
 * При удалении вершины также удаляются все полигоны, которые к ней прикреплены.
 */
public interface VertexRemover {

    /**
     * Удаляет указанные вершины из модели.
     *
     * @param model Исходная модель
     * @param vertexIndices Набор индексов вершин для удаления
     * @return Результат операции удаления
     * @throws VertexRemoverException Если возникла ошибка при удалении
     */
    VertexRemovalResult removeVertices(Model model,
                                       Set<Integer> vertexIndices,
                                       boolean clearUnused)
            throws VertexRemoverException;


    /**
     * Валидирует запрос на удаление вершин.
     *
     * @param model Модель для проверки
     * @param vertexIndices Набор индексов вершин для проверки
     * @throws VertexRemoverException Если запрос невалиден
     */
    void validateRemovalRequest(Model model, Set<Integer> vertexIndices);
}