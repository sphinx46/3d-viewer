package ru.vsu.cs.cg.controller.command.impl.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

import java.util.Optional;
import javafx.scene.control.ButtonType;

public class SceneResetCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(SceneResetCommand.class);

    private final SceneController sceneController;

    public SceneResetCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        if (sceneController.hasUnsavedChanges()) {
            Optional<ButtonType> result = DialogManager.showConfirmation(
                "Сброс сцены",
                "Сбросить все изменения в сцене? Несохраненные изменения будут потеряны."
            );

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return;
            }
        }

        sceneController.createNewScene();
        LOG.info("Сцена сброшена");
        DialogManager.showInfo("Сброс сцены", "Сцена сброшена до начального состояния");
    }

    @Override
    public String getName() {
        return "scene_reset";
    }

    @Override
    public String getDescription() {
        return "Сброс текущей сцены к начальному состоянию";
    }
}
