package ru.vsu.cs.cg.controller.command.impl.object;

import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

import java.util.Optional;

public class ObjectRenameCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectRenameCommand.class);

    private final SceneController sceneController;

    public ObjectRenameCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        if (!sceneController.hasSelectedObject()) {
            DialogManager.showError("Пожалуйста, выберите объект для переименования.");
            return;
        }

        String currentName = sceneController.getSelectedObject().getName();

        TextInputDialog dialog = new TextInputDialog(currentName);
        dialog.setTitle("Переименование объекта");
        dialog.setHeaderText("Введите новое имя для объекта");
        dialog.setContentText("Имя:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();
            sceneController.renameSelectedObject(newName);
        }
    }

    @Override
    public String getName() {
        return "object_rename";
    }

    @Override
    public String getDescription() {
        return "Переименовать выбранный объект";
    }
}
