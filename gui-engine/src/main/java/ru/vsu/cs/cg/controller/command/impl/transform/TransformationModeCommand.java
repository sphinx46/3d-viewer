package ru.vsu.cs.cg.controller.command.impl.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.enums.TransformationMode;

public class TransformationModeCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(TransformationModeCommand.class);

    private final SceneController sceneController;
    private final TransformationMode mode;

    public TransformationModeCommand(SceneController sceneController, TransformationMode mode) {
        this.sceneController = sceneController;
        this.mode = mode;
    }

    @Override
    public void execute() {
        sceneController.setTransformationMode(mode);
        LOG.info("Режим трансформации изменен: {}", mode);
    }

    @Override
    public String getName() {
        return "transform_mode_" + mode.name().toLowerCase();
    }

    @Override
    public String getDescription() {
        return "Установить режим трансформации: " + mode;
    }
}
