package ru.vsu.cs.cg.controller.command.impl.file;

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

public class FileOpenCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(FileOpenCommand.class);

    private final Stage stage;
    private final SceneController sceneController;
    private final RecentFilesCacheService recentFilesService;

    public FileOpenCommand(Stage stage, SceneController sceneController, RecentFilesCacheService recentFilesService) {
        this.stage = stage;
        this.sceneController = sceneController;
        this.recentFilesService = recentFilesService;
    }

    @Override
    public void execute() {
        DialogManager.showOpenFileDialog(stage).ifPresent(file -> {
            processFile(file.getAbsolutePath());
        });
    }

    private void processFile(String filePath) {
        try {
            if (!PathManager.isSupportedFileFormat(filePath)) {
                DialogManager.showError("Неподдерживаемый формат файла");
                return;
            }

            if (isSceneFile(filePath)) {
                openSceneFile(filePath);
            } else {
                openModelFile(filePath);
            }
        } catch (Exception e) {
            LOG.error("Ошибка обработки файла: {}", e.getMessage());
            DialogManager.showError("Ошибка открытия файла: " + e.getMessage());
        }
    }

    private boolean isSceneFile(String filePath) {
        return PathManager.isSupportedSceneFormat(filePath);
    }

    public void openSceneFile(String filePath) {
        try {
            if (sceneController.hasUnsavedChanges() && !DialogManager.confirmUnsavedChanges()) {
                return;
            }

            PathManager.validatePathForRead(filePath);
            sceneController.loadScene(filePath);

            recentFilesService.addFile(filePath);
            CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

            LOG.info("Сцена загружена из файла: {}", PathManager.getFileNameWithoutExtension(filePath));
            DialogManager.showSceneLoadSuccess("Сцена загружена: " + PathManager.getFileNameWithoutExtension(filePath));
            RecentFilesUpdateManager.getInstance().notifyRecentFilesUpdated();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки сцены: {}", e.getMessage());
            DialogManager.showError("Ошибка загрузки сцены: " + e.getMessage());
        }
    }

    public void openModelFile(String filePath) {
        try {
            sceneController.addModelToScene(filePath);

            recentFilesService.addFile(filePath);
            CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

            LOG.info("Модель загружена: {}", PathManager.getFileNameWithoutExtension(filePath));
            DialogManager.showModelLoadSuccess("Модель добавлена: " + PathManager.getFileNameWithoutExtension(filePath));
            RecentFilesUpdateManager.getInstance().notifyRecentFilesUpdated();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки модели: {}", e.getMessage());
            DialogManager.showError("Ошибка добавления модели: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "file_open";
    }

    @Override
    public String getDescription() {
        return "Открытие файла (сцена или 3D модель)";
    }
}
