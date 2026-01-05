package ru.vsu.cs.cg.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public final class DialogManager {

    private static final Logger LOG = LoggerFactory.getLogger(DialogManager.class);

    private DialogManager() {
    }

    public static void showSuccess(String message) {
        showDialog(Alert.AlertType.INFORMATION, MessageConstants.MODEL_LOAD_SUCCESS, message);
        LOG.info("Показано сообщение об успехе: {}", message);
    }

    public static void showError(String message) {
        showDialog(Alert.AlertType.ERROR, MessageConstants.MODEL_LOAD_ERROR, message);
        LOG.error("Показано сообщение об ошибке: {}", message);
    }

    public static void showInfo(String title, String message) {
        showDialog(Alert.AlertType.INFORMATION, title, message);
        LOG.info("Показано информационное сообщение: {} - {}", title, message);
    }

    public static Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupDialog(alert, title, message);

        LOG.debug("Показан диалог подтверждения: {} - {}", title, message);
        return alert.showAndWait();
    }

    public static Optional<File> showSaveDialog(Stage ownerStage) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Сохранить объект как");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("OBJ файлы", "*.obj")
        );
        fileChooser.setInitialFileName("custom_object.obj");

        File file = fileChooser.showSaveDialog(ownerStage);

        if (file != null) {
            String fileName = PathValidator.getFileNameWithoutExtension(file.getAbsolutePath());
            LOG.debug("Пользователь выбрал файл для сохранения: {}", fileName);
        } else {
            LOG.debug("Сохранение файла отменено пользователем");
        }

        return Optional.ofNullable(file);
    }

    private static void showDialog(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        setupDialog(alert, title, message);
        alert.showAndWait();
    }

    private static void setupDialog(Alert alert, String title, String message) {
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setResizable(false);
    }
}
