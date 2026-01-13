package ru.vsu.cs.cg.controller.command.impl.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

public class SceneNewCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(SceneNewCommand.class);

    private final SceneController sceneController;

    public SceneNewCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        if (sceneController.hasUnsavedChanges() && !DialogManager.confirmUnsavedChanges()) {
            return;
        }

        sceneController.createNewScene();
        LOG.info("Создана новая сцена");
        DialogManager.showInfo("Новая сцена", "Создана новая сцена");
    }

    @Override
    public String getName() {
        return "scene_new";
    }

    @Override
    public String getDescription() {
        return "Создание новой сцены";
    }
}
