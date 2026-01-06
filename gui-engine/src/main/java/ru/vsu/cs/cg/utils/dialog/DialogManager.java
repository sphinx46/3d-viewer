package ru.vsu.cs.cg.utils.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.utils.constants.MessageConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public final class DialogManager {

    private static final Logger LOG = LoggerFactory.getLogger(DialogManager.class);
    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private static final String MODEL_FILE_PREFIX = "model_";
    private static final String SCENE_FILE_PREFIX = "scene_";
    private static final String SCREENSHOT_FILE_PREFIX = "screenshot_";

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

    public static Optional<File> showOpenSceneDialog(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть сцену");

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("3D сцены", "*.3dscene"),
            new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        File file = fileChooser.showOpenDialog(ownerStage);

        if (file != null) {
            LOG.debug("Пользователь выбрал файл сцены для открытия: {}", file.getName());
        } else {
            LOG.debug("Открытие файла сцены отменено пользователем");
        }
        return Optional.ofNullable(file);
    }

    public static Optional<File> showSaveSceneDialog(Stage ownerStage, String defaultSceneName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить сцену");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("3D сцены", "*.3dscene")
        );

        String fileName = defaultSceneName != null ?
            defaultSceneName + ".3dscene" :
            generateFileName(SCENE_FILE_PREFIX, ".3dscene");
        fileChooser.setInitialFileName(fileName);

        File file = fileChooser.showSaveDialog(ownerStage);

        if (file != null) {
            LOG.debug("Пользователь выбрал файл для сохранения сцены: {}", file.getName());
        } else {
            LOG.debug("Сохранение сцены отменено пользователем");
        }

        return Optional.ofNullable(file);
    }

    public static Optional<File> showSaveDialog(Stage ownerStage, String title, String filePrefix, String extension) {
        FileChooser fileChooser = createFileChooser(title, filePrefix, extension);
        File file = fileChooser.showSaveDialog(ownerStage);

        if (file != null) {
            LOG.debug("Пользователь выбрал файл для сохранения: {}", file.getName());
        } else {
            LOG.debug("Сохранение файла отменено пользователем");
        }

        return Optional.ofNullable(file);
    }

    public static Optional<File> showOpenModelDialog(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть модель");

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("OBJ файлы", "*.obj"),
            new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        File file = fileChooser.showOpenDialog(ownerStage);

        if (file != null) {
            LOG.debug("Пользователь выбрал файл модели для открытия: {}", file.getName());
        } else {
            LOG.debug("Открытие файла модели отменено пользователем");
        }
        return Optional.ofNullable(file);
    }

    public static Optional<File> showSaveModelDialog(Stage ownerStage) {
        return showSaveDialog(ownerStage, "Сохранить модель", MODEL_FILE_PREFIX, "*.obj");
    }

    public static Optional<File> showSaveScreenshotDialog(Stage ownerStage) {
        return showSaveDialog(ownerStage, "Сохранить скриншот", SCREENSHOT_FILE_PREFIX, "*.png");
    }

    private static FileChooser createFileChooser(String title, String filePrefix, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        if ("*.obj".equals(extension)) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OBJ файлы", extension)
            );
        } else if ("*.png".equals(extension)) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG изображения", extension)
            );
        }

        fileChooser.setInitialFileName(generateFileName(filePrefix, extension));
        return fileChooser;
    }

    private static String generateFileName(String prefix, String extension) {
        String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        return prefix + timestamp + extension.substring(1);
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
