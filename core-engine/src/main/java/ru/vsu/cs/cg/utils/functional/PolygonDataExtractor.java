package ru.vsu.cs.cg.utils.functional;

import ru.vsu.cs.cg.model.Polygon;
import java.util.List;

/**
 * Функциональный интерфейс для извлечения данных из полигона.
 */
@FunctionalInterface
public interface PolygonDataExtractor {
    /**
     * Извлекает список индексов из полигона.
     *
     * @param polygon Полигон для извлечения данных
     * @return Список индексов из полигона
     */
    List<Integer> extract(Polygon polygon);
}