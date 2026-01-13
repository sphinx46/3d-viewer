package ru.vsu.cs.cg.controller.command.impl.scene;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.file.PathManager;

public class SceneOpenCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(SceneOpenCommand.class);

    private final Stage stage;
    private final SceneController sceneController;
    private final RecentFilesCacheService recentFilesService;

    public SceneOpenCommand(Stage stage, SceneController sceneController, RecentFilesCacheService recentFilesService) {
        this.stage = stage;
        this.sceneController = sceneController;
        this.recentFilesService = recentFilesService;
    }

    @Override
    public void execute() {
        if (sceneController.hasUnsavedChanges() && !DialogManager.confirmUnsavedChanges()) {
            return;
        }

        DialogManager.showOpenSceneDialog(stage).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();

                if (!PathManager.isSupportedSceneFormat(filePath)) {
                    DialogManager.showError("Неподдерживаемый формат сцены");
                    return;
                }

                sceneController.loadScene(filePath);
                recentFilesService.addFile(filePath);
                CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

                LOG.info("Сцена загружена из файла: {}", file.getName());
                DialogManager.showSuccess("Сцена загружена: " + file.getName());
            } catch (Exception e) {
                LOG.error("Ошибка загрузки сцены: {}", e.getMessage());
                DialogManager.showError("Ошибка загрузки сцены: " + e.getMessage());
            }
        });
    }

    @Override
    public String getName() {
        return "scene_open";
    }

    @Override
    public String getDescription() {
        return "Открытие сцены из файла";
    }
}
