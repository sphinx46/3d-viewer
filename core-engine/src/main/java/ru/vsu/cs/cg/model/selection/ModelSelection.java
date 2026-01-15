package ru.vsu.cs.cg.model.selection;

import java.util.HashSet;
import java.util.Set;

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
}
