package ru.vsu.cs.cg.controller.command.impl.visualization;

import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;

public class AxisToggleCommand implements Command {
    private final SceneController sceneController;

    public AxisToggleCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        sceneController.toggleAxisVisibility();
    }

    @Override
    public String getName() {
        return "axis_toggle";
    }

    @Override
    public String getDescription() {
        return "Переключение отображения осей XYZ для выбранного объекта";
    }
}
