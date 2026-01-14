package ru.vsu.cs.cg.controller.command.impl.scene;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.file.PathManager;

import java.io.File;

public class SceneSaveCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(SceneSaveCommand.class);

    private final Stage stage;
    private final SceneController sceneController;
    private final RecentFilesCacheService recentFilesService;

    public SceneSaveCommand(Stage stage, SceneController sceneController,
                            RecentFilesCacheService recentFilesService) {
        this.stage = stage;
        this.sceneController = sceneController;
        this.recentFilesService = recentFilesService;
    }

    @Override
    public void execute() {
        try {
            String currentFilePath = sceneController.getCurrentSceneFilePath();

            if (currentFilePath != null) {
                if (!PathManager.isSupportedSceneFormat(currentFilePath)) {
                    DialogManager.showError("Неподдерживаемый формат сцены");
                    saveWithDialog();
                    return;
                }
                sceneController.saveScene(currentFilePath);
                DialogManager.showSceneSaveSuccess("Сцена сохранена");
            } else {
                saveWithDialog();
            }
        } catch (Exception e) {
            LOG.error("Ошибка сохранения сцены: {}", e.getMessage());
            DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
        }
    }

    private void saveWithDialog() {
        DialogManager.showSaveSceneDialog(stage, sceneController.getCurrentScene().getName()).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();

                if (!PathManager.isSupportedSceneFormat(filePath)) {
                    filePath = PathManager.ensureExtension(filePath, ".3dscene");
                    file = new File(filePath);
                }

                sceneController.saveScene(filePath);
                recentFilesService.addFile(filePath);
                CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

                LOG.info("Сцена сохранена: {}", file.getName());
                DialogManager.showSceneSaveSuccess("Сцена сохранена: " + file.getName());
            } catch (Exception e) {
                LOG.error("Ошибка сохранения сцены: {}", e.getMessage());
                DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
            }
        });
    }

    @Override
    public String getName() {
        return "scene_save";
    }

    @Override
    public String getDescription() {
        return "Сохранение сцены";
    }
}
