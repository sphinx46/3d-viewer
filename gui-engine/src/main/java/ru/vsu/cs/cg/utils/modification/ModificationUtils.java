package ru.vsu.cs.cg.utils.modification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.selection.ModelSelection;
import ru.vsu.cs.cg.utils.parser.IndexParser;

import java.util.Set;

public final class ModificationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ModificationUtils.class);

    private ModificationUtils() {
    }

    public static void updateSelection(String indicesInput, Model model, boolean isVertices, boolean select)
        throws IndexOutOfBoundsException {
        try {
            int maxIndex = isVertices ? model.getVertices().size() : model.getPolygons().size();
            Set<Integer> indices = IndexParser.parseAndValidateIndices(indicesInput, maxIndex);
            ModelSelection selection = model.getSelection();

            if (isVertices) {
                handleVertexSelection(selection, indices, select);
            } else {
                handleTriangleSelection(selection, indices, select);
            }

            String action = select ? "выделено" : "снято выделение";
            String type = isVertices ? "вершин" : "треугольников";
            LOG.info("{} с {} {}", action, indices.size(), type);

        } catch (IllegalArgumentException e) {
            LOG.error("Ошибка {} {}: {}", select ? "выделения" : "снятия выделения",
                isVertices ? "вершин" : "треугольников", e.getMessage());
        }
    }

    private static void handleVertexSelection(ModelSelection selection, Set<Integer> indices, boolean select) {
        if (select) {
            selection.clearVertexSelection();
            indices.forEach(selection::selectVertex);
        } else {
            if (indices.isEmpty()) {
                selection.clearVertexSelection();
            } else {
                indices.forEach(index -> {
                    if (selection.isVertexSelected(index)) {
                        selection.deselectVertex(index);
                    }
                });
            }
        }
    }

    private static void handleTriangleSelection(ModelSelection selection, Set<Integer> indices, boolean select) {
        if (select) {
            selection.clearTriangles();
            indices.forEach(selection::addSelectedTriangle);
        } else {
            if (indices.isEmpty()) {
                selection.clearTriangles();
            } else {
                indices.forEach(index -> {
                    if (selection.getSelectedTriangles().contains(index)) {
                        selection.removeSelectedTriangle(index);
                    }
                });
            }
        }
    }
}
