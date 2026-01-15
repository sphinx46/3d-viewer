package ru.vsu.cs.cg.controller.command.impl.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;

public class DefaultModelAddCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultModelAddCommand.class);

    private final SceneController sceneController;
    private final DefaultModelLoader.ModelType modelType;

    public DefaultModelAddCommand(SceneController sceneController, DefaultModelLoader.ModelType modelType) {
        this.sceneController = sceneController;
        this.modelType = modelType;
    }

    @Override
    public void execute() {
        try {
            sceneController.addDefaultModelToScene(modelType);
            LOG.info("Добавлена стандартная модель: {}", modelType.getDisplayName());
            DialogManager.showModelLoadSuccess("Модель '" + modelType.getDisplayName() + "' добавлена");
        } catch (Exception e) {
            LOG.error("Ошибка добавления стандартной модели '{}': {}", modelType.getDisplayName(), e.getMessage());
            DialogManager.showError("Ошибка добавления модели: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "model_add_" + modelType.name().toLowerCase();
    }

    @Override
    public String getDescription() {
        return "Добавление стандартной модели: " + modelType.getDisplayName();
    }
}
