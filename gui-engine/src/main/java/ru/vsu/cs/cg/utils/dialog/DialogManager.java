package ru.vsu.cs.cg.utils.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.vsu.cs.cg.utils.file.PathManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public final class DialogManager {
    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private static final String MODEL_FILE_PREFIX = "model_";
    private static final String SCENE_FILE_PREFIX = "scene_";
    private static final String SCREENSHOT_FILE_PREFIX = "screenshot_";

    private DialogManager() {
    }

    public static void showSceneSaveSuccess(String message) {
        showDialog(Alert.AlertType.INFORMATION, SCENE_SAVE_SUCCESS, message);
    }

    public static void showModelSaveSuccess(String message) {
        showDialog(Alert.AlertType.INFORMATION, MODEL_SAVE_SUCCESS, message);
    }

    public static void showModelLoadSuccess(String message) {
        showDialog(Alert.AlertType.INFORMATION, MODEL_LOAD_SUCCESS, message);
    }

    public static void showSceneLoadSuccess(String message) {
        showDialog(Alert.AlertType.INFORMATION, SCENE_LOAD_SUCCESS, message);
    }

    public static Optional<File> showOpenFileDialog(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть файл");

        FileChooser.ExtensionFilter sceneFilter = new FileChooser.ExtensionFilter(
            "Файлы сцены (.3dscene)", "*.3dscene"
        );
        FileChooser.ExtensionFilter modelFilter = new FileChooser.ExtensionFilter(
            "3D модели (.obj)", "*.obj"
        );
        FileChooser.ExtensionFilter allSupportedFilter = new FileChooser.ExtensionFilter(
            "Все поддерживаемые файлы",
            "*.3dscene", "*.obj"
        );

        fileChooser.getExtensionFilters().addAll(sceneFilter, modelFilter, allSupportedFilter);
        fileChooser.setSelectedExtensionFilter(allSupportedFilter);

        File file = fileChooser.showOpenDialog(ownerStage);
        if (file != null && !PathManager.isSupportedFileFormat(file.getAbsolutePath())) {
            showError("Неподдерживаемый формат файла. Поддерживаемые форматы: .3dscene, .obj");
            return Optional.empty();
        }
        return Optional.ofNullable(file);
    }

    public static Optional<File> showOpenJsonSceneDialog(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Импорт сцены из JSON");

        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter(
            "JSON файлы сцены (.json)", "*.json"
        );

        fileChooser.getExtensionFilters().add(jsonFilter);
        fileChooser.setSelectedExtensionFilter(jsonFilter);

        return Optional.ofNullable(fileChooser.showOpenDialog(ownerStage));
    }

    public static Optional<File> showSaveJsonSceneDialog(Stage ownerStage, String defaultSceneName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт сцены в JSON");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON файлы (.json)", "*.json")
        );

        String fileName = defaultSceneName != null ?
            defaultSceneName + ".json" :
            generateFileName(SCENE_FILE_PREFIX, ".json");
        fileChooser.setInitialFileName(fileName);

        File file = fileChooser.showSaveDialog(ownerStage);
        if (file != null) {
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".json")) {
                file = new File(path + ".json");
            }
        }
        return Optional.ofNullable(file);
    }

    public static Optional<File> showSaveSceneDialog(Stage ownerStage, String defaultSceneName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить сцену");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("3D сцены (.3dscene)", "*.3dscene")
        );

        String fileName = defaultSceneName != null ?
            defaultSceneName + ".3dscene" :
            generateFileName(SCENE_FILE_PREFIX, ".3dscene");
        fileChooser.setInitialFileName(fileName);

        File file = fileChooser.showSaveDialog(ownerStage);
        if (file != null) {
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".3dscene")) {
                file = new File(path + ".3dscene");
            }
        }
        return Optional.ofNullable(file);
    }

    public static Optional<File> showSaveModelDialog(Stage ownerStage) {
        return showSaveDialog(ownerStage, "Сохранить модель", MODEL_FILE_PREFIX, "*.obj");
    }

    public static Optional<File> showSaveScreenshotDialog(Stage ownerStage) {
        return showSaveDialog(ownerStage, "Сохранить скриншот", SCREENSHOT_FILE_PREFIX, "*.png");
    }

    private static Optional<File> showSaveDialog(Stage ownerStage, String title, String filePrefix, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(extension.substring(2).toUpperCase() + " файлы", extension)
        );
        fileChooser.setInitialFileName(generateFileName(filePrefix, extension));
        return Optional.ofNullable(fileChooser.showSaveDialog(ownerStage));
    }

    public static void showError(String message) {
        showDialog(Alert.AlertType.ERROR, "Ошибка", message);
    }

    public static void showInfo(String title, String message) {
        showDialog(Alert.AlertType.INFORMATION, title, message);
    }

    public static Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupDialog(alert, title, message);
        return alert.showAndWait();
    }

    public static boolean confirmUnsavedChanges() {
        Optional<ButtonType> result = showConfirmation(
            "Несохраненные изменения",
            "В сцене есть несохраненные изменения. Вы уверены, что хотите продолжить?"
        );
        return result.isPresent() && result.get() == ButtonType.OK;
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
