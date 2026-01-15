package ru.vsu.cs.cg.controller.command.impl.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

public class ObjectCopyCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectCopyCommand.class);

    private final SceneController sceneController;

    public ObjectCopyCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        try {
            if (!sceneController.hasSelectedObject()) {
                DialogManager.showError("Нет выбранного объекта");
                return;
            }

            sceneController.copySelectedObject();
            LOG.info("Объект скопирован в буфер обмена");
            DialogManager.showInfo("Копирование", "Объект скопирован в буфер");
        } catch (Exception e) {
            LOG.error("Ошибка копирования объекта: {}", e.getMessage());
            DialogManager.showError("Ошибка копирования объекта: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "object_copy";
    }

    @Override
    public String getDescription() {
        return "Копирование выбранного объекта в буфер обмена";
    }
}
