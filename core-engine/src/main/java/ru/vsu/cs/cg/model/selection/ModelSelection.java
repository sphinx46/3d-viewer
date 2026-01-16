package ru.vsu.cs.cg.model.selection;

import java.util.*;

public class ModelSelection {
    private final Set<Integer> selectedVertices = new HashSet<>();
    private final Set<Integer> selectedPolygons = new HashSet<>();

    public Set<Integer> getSelectedVertices() {
        return new HashSet<>(selectedVertices);
    }

    public Set<Integer> getSelectedPolygons() {
        return new HashSet<>(selectedPolygons);
    }

    public void selectVertex(int index) {
        selectedVertices.add(index);
    }

    public void selectPolygon(int index) {
        selectedPolygons.add(index);
    }

    public void deselectVertex(int index) {
        selectedVertices.remove(index);
    }

    public void deselectPolygon(int index) {
        selectedPolygons.remove(index);
    }

    public void clearVertexSelection() {
        selectedVertices.clear();
    }

    public void clearPolygonSelection() {
        selectedPolygons.clear();
    }

    public void clearAll() {
        clearVertexSelection();
        clearPolygonSelection();
    }

    public boolean isVertexSelected(int index) {
        return selectedVertices.contains(index);
    }

    public boolean isPolygonSelected(int index) {
        return selectedPolygons.contains(index);
    }

    public boolean hasSelectedVertices() {
        return !selectedVertices.isEmpty();
    }

    public boolean hasSelectedPolygons() {
        return !selectedPolygons.isEmpty();
    }

    public int getSelectedVerticesCount() {
        return selectedVertices.size();
    }

    public int getSelectedPolygonsCount() {
        return selectedPolygons.size();
    }

    /**
     * Корректирует индексы выделения после удаления элементов
     * @param removedVertices Набор удаленных индексов вершин
     * @param removedPolygons Набор удаленных индексов полигонов
     */
    public void adjustSelectionAfterRemoval(Set<Integer> removedVertices, Set<Integer> removedPolygons) {
        adjustIndicesSet(selectedVertices, removedVertices);
        adjustIndicesSet(selectedPolygons, removedPolygons);
    }

    /**
     * Корректирует индексы выделения после удаления полигонов
     * @param removedPolygons Набор удаленных индексов полигонов
     */
    public void adjustSelectionAfterPolygonRemoval(Set<Integer> removedPolygons) {
        adjustIndicesSet(selectedPolygons, removedPolygons);
    }

    /**
     * Корректирует индексы выделения после удаления вершин
     * @param removedVertices Набор удаленных индексов вершин
     */
    public void adjustSelectionAfterVertexRemoval(Set<Integer> removedVertices) {
        adjustIndicesSet(selectedVertices, removedVertices);
    }

    private void adjustIndicesSet(Set<Integer> indices, Set<Integer> removedIndices) {
        if (indices.isEmpty() || removedIndices.isEmpty()) {
            return;
        }

        Set<Integer> newIndices = new HashSet<>();
        List<Integer> sortedRemoved = new ArrayList<>(removedIndices);
        Collections.sort(sortedRemoved);

        for (Integer index : indices) {
            int shift = 0;
            boolean isRemoved = false;

            for (Integer removed : sortedRemoved) {
                if (index > removed) {
                    shift++;
                } else if (index.equals(removed)) {
                    isRemoved = true;
                    break;
                }
            }

            if (!isRemoved) {
                newIndices.add(index - shift);
            }
        }

        indices.clear();
        indices.addAll(newIndices);
    }
}
