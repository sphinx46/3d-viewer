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

import java.io.File;

public class SceneSaveCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(SceneSaveCommand.class);

    private final Stage stage;
    private final SceneController sceneController;
    private final RecentFilesCacheService recentFilesService;
    private final boolean saveAs;

    public SceneSaveCommand(Stage stage, SceneController sceneController,
                            RecentFilesCacheService recentFilesService, boolean saveAs) {
        this.stage = stage;
        this.sceneController = sceneController;
        this.recentFilesService = recentFilesService;
        this.saveAs = saveAs;
    }

    @Override
    public void execute() {
        try {
            if (!saveAs && sceneController.getCurrentSceneFilePath() != null) {
                String filePath = sceneController.getCurrentSceneFilePath();
                if (!PathManager.isSupportedSceneFormat(filePath)) {
                    DialogManager.showError("Неподдерживаемый формат сцены");
                    saveSceneAs();
                    return;
                }
                sceneController.saveScene(filePath);
                DialogManager.showSceneSaveSuccess("Сцена сохранена");
            } else {
                saveSceneAs();
            }
        } catch (Exception e) {
            LOG.error("Ошибка сохранения сцены: {}", e.getMessage());
            DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
        }
    }

    private void saveSceneAs() {
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
        return saveAs ? "scene_save_as" : "scene_save";
    }

    @Override
    public String getDescription() {
        return saveAs ? "Сохранение сцены в новый файл" : "Сохранение текущей сцены";
    }
}
