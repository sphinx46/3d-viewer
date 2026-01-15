package ru.vsu.cs.cg.controller.command.impl.scene;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.events.RecentFilesUpdateManager;
import ru.vsu.cs.cg.utils.file.PathManager;

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
            if (sceneController.getCurrentScene().isEmpty()) {
                DialogManager.showError("Невозможно сохранить пустую сцену");
                return;
            }

            saveWithDialog();

        } catch (Exception e) {
            LOG.error("Ошибка сохранения сцены: {}", e.getMessage());
            DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
        }
    }

    private void saveSceneToPath(String filePath) {
        try {
            if (!PathManager.isSupportedSceneFormat(filePath)) {
                DialogManager.showError("Неподдерживаемый формат сцены");
                saveWithDialog();
                return;
            }

            LOG.info("Сохранение сцены в файл: {}", filePath);
            sceneController.saveScene(filePath);
            recentFilesService.addFile(filePath);
            CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

            LOG.info("Сцена сохранена в файл: {}", PathManager.getFileNameWithoutExtension(filePath));
            DialogManager.showSceneSaveSuccess("Сцена сохранена");

            RecentFilesUpdateManager.getInstance().notifyRecentFilesUpdated();

        } catch (Exception e) {
            LOG.error("Ошибка сохранения сцены в файл {}: {}", filePath, e.getMessage());
            DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
            saveWithDialog();
        }
    }

    private void saveWithDialog() {
        DialogManager.showSaveSceneDialog(stage, sceneController.getCurrentScene().getName()).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();

                if (!PathManager.isSupportedSceneFormat(filePath)) {
                    filePath = PathManager.ensureExtension(filePath, ".3dscene");
                }

                saveSceneToPath(filePath);

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
