package ru.vsu.cs.cg.controller.command.impl.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

import java.util.Optional;
import javafx.scene.control.ButtonType;

public class ObjectDeleteCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectDeleteCommand.class);

    private final SceneController sceneController;

    public ObjectDeleteCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        if (!sceneController.hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        String objectName = sceneController.getSelectedObject().getName();
        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Удаление объекта",
            "Удалить объект '" + objectName + "'?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            sceneController.removeSelectedObject();
            LOG.info("Объект удален: {}", objectName);
            DialogManager.showInfo("Удаление", "Объект '" + objectName + "' удален");
        }
    }

    @Override
    public String getName() {
        return "object_delete";
    }

    @Override
    public String getDescription() {
        return "Удаление выбранного объекта из сцены";
    }
}
