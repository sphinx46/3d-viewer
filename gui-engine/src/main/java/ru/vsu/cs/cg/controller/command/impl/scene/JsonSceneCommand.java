package ru.vsu.cs.cg.controller.command.impl.scene;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.service.SceneService;
import ru.vsu.cs.cg.service.impl.SceneServiceImpl;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.file.PathManager;

import java.io.File;
import java.util.Optional;

public class JsonSceneCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(JsonSceneCommand.class);

    public enum Mode {
        IMPORT,
        EXPORT
    }

    private final Stage stage;
    private final SceneController sceneController;
    private final RecentFilesCacheService recentFilesService;
    private final Mode mode;
    private final SceneService sceneService;

    public JsonSceneCommand(Stage stage, SceneController sceneController,
                            RecentFilesCacheService recentFilesService, Mode mode) {
        this.stage = stage;
        this.sceneController = sceneController;
        this.recentFilesService = recentFilesService;
        this.mode = mode;
        this.sceneService = new SceneServiceImpl(null);
    }

    @Override
    public void execute() {
        switch (mode) {
            case IMPORT -> importScene();
            case EXPORT -> exportScene();
        }
    }

    private void importScene() {
        if (sceneController.hasUnsavedChanges()) {
            Optional<javafx.scene.control.ButtonType> result = DialogManager.showConfirmation(
                "Несохраненные изменения",
                "В текущей сцене есть несохраненные изменения. Продолжить?"
            );

            if (result.isEmpty() || result.get() == javafx.scene.control.ButtonType.CANCEL) {
                return;
            }
        }

        DialogManager.showOpenJsonSceneDialog(stage).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();
                PathManager.validatePathForRead(filePath);

                if (!PathManager.isImportOrExportSceneFormat(filePath)) {
                    DialogManager.showError("Для импорта поддерживается только формат JSON (.json)");
                    return;
                }

                LOG.info("Импорт сцены из JSON файла: {}", filePath);
                sceneController.loadScene(filePath);
                recentFilesService.addFile(filePath);
                CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

                LOG.info("Сцена успешно импортирована из JSON файла: {}", file.getName());
                DialogManager.showSceneLoadSuccess("Сцена успешно импортирована из JSON файла");

            } catch (Exception e) {
                LOG.error("Ошибка импорта сцены из JSON файла: {}", e.getMessage());
                DialogManager.showError("Ошибка импорта сцены: " + e.getMessage());
            }
        });
    }

    private void exportScene() {
        if (sceneController.getCurrentScene().isEmpty()) {
            DialogManager.showError("Невозможно экспортировать пустую сцену");
            return;
        }

        DialogManager.showSaveJsonSceneDialog(stage, sceneController.getCurrentScene().getName()).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();

                if (!PathManager.isImportOrExportSceneFormat(filePath)) {
                    filePath = PathManager.ensureExtension(filePath, ".json");
                    file = new File(filePath);
                }

                LOG.info("Экспорт сцены в JSON файл: {}", filePath);
                sceneService.saveScene(sceneController.getCurrentScene(), filePath);
                recentFilesService.addFile(filePath);
                CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

                LOG.info("Сцена успешно экспортирована в JSON файл: {}", file.getName());
                DialogManager.showSceneSaveSuccess("Сцена успешно экспортирована в JSON файл");

            } catch (Exception e) {
                LOG.error("Ошибка экспорта сцены в JSON файл: {}", e.getMessage());
                DialogManager.showError("Ошибка экспорта сцены: " + e.getMessage());
            }
        });
    }

    @Override
    public String getName() {
        return "scene_json_" + mode.name().toLowerCase();
    }

    @Override
    public String getDescription() {
        return mode == Mode.IMPORT ? "Импорт сцены из JSON файла" : "Экспорт сцены в JSON файл";
    }
}
