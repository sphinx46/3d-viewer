package ru.vsu.cs.cg.controller.command.impl.visualization;

import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;

public class GridToggleCommand implements Command {
    private final SceneController sceneController;

    public GridToggleCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        sceneController.toggleGridVisibility();
    }

    @Override
    public String getName() {
        return "grid_toggle";
    }

    @Override
    public String getDescription() {
        return "Переключение отображения фоновой сетки";
    }
}
