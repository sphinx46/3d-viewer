package ru.vsu.cs.cg.controller.command.impl.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;

public class CustomObjectCreateCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(CustomObjectCreateCommand.class);

    private final SceneController sceneController;

    public CustomObjectCreateCommand(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @Override
    public void execute() {
        try {
            sceneController.addDefaultModelToScene(DefaultModelLoader.ModelType.CUBE);
            LOG.info("Создан пользовательский объект");
            DialogManager.showInfo("Пользовательский объект", "Создан новый объект для редактирования");
        } catch (Exception e) {
            LOG.error("Ошибка создания пользовательского объекта: {}", e.getMessage());
            DialogManager.showError("Ошибка создания объекта: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "custom_object_create";
    }

    @Override
    public String getDescription() {
        return "Создание нового пользовательского объекта";
    }
}
