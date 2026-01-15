package ru.vsu.cs.cg.model.selection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ModelSelection {
    private final Set<Integer> selectedVertices = new HashSet<>();
    private static Set<Integer> selectedTriangles = new HashSet<>();

    public void addSelectedTriangle(int triangleIndex) {
        selectedTriangles.add(triangleIndex);
    }

    public void clearTriangles() {
        selectedTriangles.clear();
    }

    public Set<Integer> getSelectedTriangles() {
        return Collections.unmodifiableSet(selectedTriangles);
    }

    public boolean hasSelectedTriangles() {
        return !selectedTriangles.isEmpty();
    }

    public Set<Integer> getSelectedVertices() {
        return new HashSet<>(selectedVertices);
    }


    public void selectVertex(int index) {
        selectedVertices.add(index);
    }


    public void deselectVertex(int index) {
        selectedVertices.remove(index);
    }


    public void clearVertexSelection() {
        selectedVertices.clear();
    }

    public void clearAll() {
        clearVertexSelection();
        clearTriangles();
    }

    public boolean isVertexSelected(int index) {
        return selectedVertices.contains(index);
    }


    public boolean hasSelectedVertices() {
        return !selectedVertices.isEmpty();
    }


    public int getSelectedVerticesCount() {
        return selectedVertices.size();
    }


    public void removeSelectedTriangle(int triangleIndex) {
        selectedTriangles.remove(triangleIndex);

    }

    public void clearAllTriangles() {
        selectedTriangles.clear();
    }

    public static void adjustTriangleIndicesAfterDeletion(Set<Integer> deletedIndices) {
        if (selectedTriangles.isEmpty() || deletedIndices.isEmpty()) {
            return;
        }

        Set<Integer> newSelectedTriangles = new HashSet<>();
        for (Integer triangleIndex : selectedTriangles) {
            int shiftCount = 0;
            for (Integer deletedIndex : deletedIndices) {
                if (deletedIndex <= triangleIndex) {
                    shiftCount++;
                }
            }
            int adjustedIndex = triangleIndex - shiftCount;
            if (adjustedIndex >= 0 && !deletedIndices.contains(triangleIndex)) {
                newSelectedTriangles.add(adjustedIndex);
            }
        }
        selectedTriangles = newSelectedTriangles;
    }
}
