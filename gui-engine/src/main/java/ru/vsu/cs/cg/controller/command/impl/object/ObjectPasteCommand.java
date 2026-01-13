package ru.vsu.cs.cg.controller.command.impl.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

public class ObjectPasteCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectPasteCommand.class);

    private final SceneController sceneController;

    public ObjectPasteCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        try {
            sceneController.pasteCopiedObject();
            LOG.info("Объект вставлен из буфера обмена");
            DialogManager.showInfo("Вставка", "Объект вставлен в сцену");
        } catch (Exception e) {
            LOG.error("Ошибка вставки объекта: {}", e.getMessage());
            DialogManager.showError("Ошибка вставки объекта: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "object_paste";
    }

    @Override
    public String getDescription() {
        return "Вставка объекта из буфера обмена в сцену";
    }
}
