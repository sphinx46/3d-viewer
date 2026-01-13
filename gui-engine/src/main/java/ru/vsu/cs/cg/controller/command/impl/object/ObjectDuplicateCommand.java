package ru.vsu.cs.cg.controller.command.impl.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

public class ObjectDuplicateCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectDuplicateCommand.class);

    private final SceneController sceneController;

    public ObjectDuplicateCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        try {
            if (!sceneController.hasSelectedObject()) {
                DialogManager.showError("Нет выбранного объекта");
                return;
            }

            sceneController.duplicateSelectedObject();
            LOG.info("Объект продублирован");
            DialogManager.showInfo("Дублирование", "Создана копия объекта");
        } catch (Exception e) {
            LOG.error("Ошибка дублирования объекта: {}", e.getMessage());
            DialogManager.showError("Ошибка дублирования объекта: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "object_duplicate";
    }

    @Override
    public String getDescription() {
        return "Дублирование выбранного объекта";
    }
}
