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

    public static void updateSelection(String indicesInput, Model model, boolean isVertices, boolean select) {
        try {
            int maxIndex = isVertices ? model.getVertices().size() : model.getPolygons().size();
            Set<Integer> indices = IndexParser.parseAndValidateIndices(indicesInput, maxIndex);
            ModelSelection selection = model.getSelection();

            if (isVertices) {
                handleVertexSelection(selection, indices, select);
            } else {
                handlePolygonSelection(selection, indices, select);
            }

            String action = select ? "выделено" : "снято выделение";
            String type = isVertices ? "вершин" : "полигонов";
            LOG.info("{} с {} {}", action, indices.size(), type);

        } catch (IllegalArgumentException e) {
            LOG.error("Ошибка {} {}: {}", select ? "выделения" : "снятия выделения",
                isVertices ? "вершин" : "полигонов", e.getMessage());
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

    private static void handlePolygonSelection(ModelSelection selection, Set<Integer> indices, boolean select) {
        if (select) {
            selection.clearPolygonSelection();
            indices.forEach(selection::selectPolygon);
        } else {
            if (indices.isEmpty()) {
                selection.clearPolygonSelection();
            } else {
                indices.forEach(index -> {
                    if (selection.isPolygonSelected(index)) {
                        selection.deselectPolygon(index);
                    }
                });
            }
        }
    }
}
