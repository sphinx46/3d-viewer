package ru.vsu.cs.cg.controller.command.impl.modification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.exceptions.VertexRemoverException;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.utils.constants.MessageConstants;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.parser.IndexParser;
import ru.vsu.cs.cg.vertexremover.VertexRemover;
import ru.vsu.cs.cg.vertexremover.VertexRemoverImpl;
import ru.vsu.cs.cg.vertexremover.dto.VertexRemovalResult;

import java.util.Set;

public class RemoveVerticesCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveVerticesCommand.class);

    private final SceneController sceneController;
    private final String vertexIndicesInput;
    private final boolean clearUnused;
    private final VertexRemover vertexRemover;

    public RemoveVerticesCommand(SceneController sceneController,
                                 String vertexIndicesInput,
                                 boolean clearUnused) {
        this.sceneController = sceneController;
        this.vertexIndicesInput = vertexIndicesInput;
        this.clearUnused = clearUnused;
        this.vertexRemover = new VertexRemoverImpl();
    }

    @Override
    public void execute() {
        try {
            LOG.info("Начало удаления вершин: индексы={}, очистка неиспользуемых={}",
                vertexIndicesInput, clearUnused);

            if (!sceneController.hasSelectedObject()) {
                DialogManager.showError(MessageConstants.NO_SELECTED_OBJECT);
                return;
            }

            SceneObject selectedObject = sceneController.getSelectedObject();
            Model model = selectedObject.getModel();

            Set<Integer> vertexIndices = IndexParser.parseIndices(vertexIndicesInput);

            if (vertexIndices.isEmpty()) {
                DialogManager.showError(MessageConstants.VERTICES_INDICES_INVALID);
                return;
            }

            if (!IndexParser.validateIndices(vertexIndices, model.getVertices().size())) {
                DialogManager.showError(MessageConstants.VERTICES_INDICES_INVALID);
                return;
            }

            VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, clearUnused);

            sceneController.markModelModified();
            updateModelStatistics(selectedObject);

            LOG.info("Удаление вершин завершено: удалено вершин={}, удалено полигонов={}",
                result.getRemovedVerticesCount(), result.getRemovedPolygonsCount());

            showSuccessMessage(result);

        } catch (VertexRemoverException e) {
            LOG.error("Ошибка при удалении вершин: {}", e.getMessage());
            DialogManager.showError(MessageConstants.VERTICES_REMOVE_ERROR + ": " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка при удалении вершин: {}", e.getMessage());
            DialogManager.showError(MessageConstants.VERTICES_REMOVE_ERROR);
        }
    }

    private void updateModelStatistics(SceneObject object) {
        LOG.debug("Статистика модели обновлена для объекта: {}", object.getName());
    }

    private void showSuccessMessage(VertexRemovalResult result) {
        String message = String.format(
            "Успешно удалено:\nВершин: %d\nПолигонов: %d",
            result.getRemovedVerticesCount(),
            result.getRemovedPolygonsCount()
        );

        DialogManager.showInfo("Удаление вершин", message);
    }

    @Override
    public String getName() {
        return "vertices_remove";
    }

    @Override
    public String getDescription() {
        return "Удаление вершин из выбранной модели";
    }
}
