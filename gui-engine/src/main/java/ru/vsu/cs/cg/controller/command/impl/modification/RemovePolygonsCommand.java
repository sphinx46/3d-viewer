package ru.vsu.cs.cg.controller.command.impl.modification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.utils.constants.MessageConstants;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.parser.IndexParser;

import java.util.Set;

public class RemovePolygonsCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(RemovePolygonsCommand.class);

    private final SceneController sceneController;
    private final String polygonIndicesInput;

    public RemovePolygonsCommand(SceneController sceneController, String polygonIndicesInput) {
        this.sceneController = sceneController;
        this.polygonIndicesInput = polygonIndicesInput;
    }

    @Override
    public void execute() {
        try {
            LOG.info("Начало удаления полигонов: индексы={}", polygonIndicesInput);

            if (!sceneController.hasSelectedObject()) {
                DialogManager.showError(MessageConstants.NO_SELECTED_OBJECT);
                return;
            }

            SceneObject selectedObject = sceneController.getSelectedObject();
            Model model = selectedObject.getModel();

            Set<Integer> polygonIndices = IndexParser.parseIndices(polygonIndicesInput);

            if (polygonIndices.isEmpty()) {
                DialogManager.showError(MessageConstants.POLYGONS_INDICES_INVALID);
                return;
            }

            if (!IndexParser.validateIndices(polygonIndices, model.getPolygons().size())) {
                DialogManager.showError(MessageConstants.POLYGONS_INDICES_INVALID);
                return;
            }

            removePolygonsFromModel(model, polygonIndices);

            sceneController.markModelModified();
            updateModelStatistics(selectedObject);

            LOG.info("Удаление полигонов завершено: удалено полигонов={}", polygonIndices.size());

            showSuccessMessage(polygonIndices.size());

        } catch (Exception e) {
            LOG.error("Ошибка при удалении полигонов: {}", e.getMessage());
            DialogManager.showError(MessageConstants.POLYGONS_REMOVE_ERROR);
        }
    }

    private void removePolygonsFromModel(Model model, Set<Integer> polygonIndices) {
        ru.vsu.cs.cg.utils.RemovalUtils.removePolygonsFromModel(model, polygonIndices);
    }

    private void updateModelStatistics(SceneObject object) {
        LOG.debug("Статистика модели обновлена для объекта: {}", object.getName());
    }

    private void showSuccessMessage(int removedCount) {
        String message = String.format("Успешно удалено полигонов: %d", removedCount);
        DialogManager.showInfo("Удаление полигонов", message);
    }

    @Override
    public String getName() {
        return "polygons_remove";
    }

    @Override
    public String getDescription() {
        return "Удаление полигонов из выбранной модели";
    }
}
