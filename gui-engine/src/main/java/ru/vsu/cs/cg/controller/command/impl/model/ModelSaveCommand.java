package ru.vsu.cs.cg.controller.command.impl.model;

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
import java.util.Optional;

public class ModelSaveCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ModelSaveCommand.class);

    private final Stage stage;
    private final SceneController sceneController;
    private final RecentFilesCacheService recentFilesService;

    public ModelSaveCommand(Stage stage, SceneController sceneController, RecentFilesCacheService recentFilesService) {
        this.stage = stage;
        this.sceneController = sceneController;
        this.recentFilesService = recentFilesService;
    }

    @Override
    public void execute() {
        if (!sceneController.hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта для сохранения");
            return;
        }

        if (sceneController.isModelModified()) {
            Optional<javafx.scene.control.ButtonType> result = DialogManager.showConfirmation(
                "Модель изменена",
                "Модель была изменена. Сохранить изменения?"
            );

            if (result.isEmpty() || result.get() == javafx.scene.control.ButtonType.CANCEL) {
                return;
            }
        }

        DialogManager.showSaveModelDialog(stage).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();

                if (!PathManager.isSupported3DFormat(filePath)) {
                    filePath = PathManager.ensureExtension(filePath, ".obj");
                    file = new File(filePath);
                }

                sceneController.saveSelectedModelToFile(filePath);
                recentFilesService.addFile(filePath);
                CachePersistenceManager.saveRecentFiles(recentFilesService.getRecentFiles());

                LOG.info("Модель сохранена: {}", file.getName());
                DialogManager.showModelSaveSuccess("Модель сохранена: " + file.getName());
            } catch (Exception e) {
                LOG.error("Ошибка сохранения модели: {}", e.getMessage());
                DialogManager.showError("Ошибка сохранения модели: " + e.getMessage());
            }
        });
    }

    @Override
    public String getName() {
        return "model_save";
    }

    @Override
    public String getDescription() {
        return "Сохранение выбранной модели в файл";
    }
}
