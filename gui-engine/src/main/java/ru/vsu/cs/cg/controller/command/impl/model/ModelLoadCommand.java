package ru.vsu.cs.cg.controller.command.impl.model;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.file.PathManager;

public class ModelLoadCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ModelLoadCommand.class);

    private final Stage stage;
    private final SceneController sceneController;
    private final RecentFilesCacheService recentFilesService;

    public ModelLoadCommand(Stage stage, SceneController sceneController, RecentFilesCacheService recentFilesService) {
        this.stage = stage;
        this.sceneController = sceneController;
        this.recentFilesService = recentFilesService;
    }

    @Override
    public void execute() {
        DialogManager.showOpenModelDialog(stage).ifPresent(file -> {
            loadModelFromPath(file.getAbsolutePath());
        });
    }

    public void loadModelFromPath(String filePath) {
        try {
            if (!PathManager.isSupported3DFormat(filePath)) {
                DialogManager.showError("Неподдерживаемый формат 3D модели");
                return;
            }

            sceneController.addModelToScene(filePath);
            recentFilesService.addFile(filePath);
            CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

            LOG.info("Модель загружена: {}", PathManager.getFileNameWithoutExtension(filePath));
            DialogManager.showModelLoadSuccess("Модель добавлена: " + PathManager.getFileNameWithoutExtension(filePath));
        } catch (Exception e) {
            LOG.error("Ошибка загрузки модели: {}", e.getMessage());
            DialogManager.showError("Ошибка добавления модели: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "model_load";
    }

    @Override
    public String getDescription() {
        return "Загрузка 3D модели из файла";
    }
}
