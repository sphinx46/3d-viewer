package ru.vsu.cs.cg.controller.command.impl.camera;

import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.camera.Camera;
import ru.vsu.cs.cg.scene.SceneManager;

import java.util.Set;

public class MenuCreateCameraCommand implements Command {

    private final String name;
    private final SceneController sceneController;
    private final Vector3f position;

    public MenuCreateCameraCommand(SceneController sceneController, Vector3f position, String name) {
        this.sceneController = sceneController;
        this.position = position;
        this.name = name;
    }

    @Override
    public void execute() {
        sceneController.addCamera(position);
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public String getDescription() {
        return "Добавление камеры на сцену";
    }
}
