package ru.vsu.cs.cg.utils.dialog;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.utils.validation.InputValidator;
import ru.vsu.cs.cg.utils.file.PathManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public final class ScreenshotManager {

    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotManager.class);
    private static final String PNG_FORMAT = "png";

    private ScreenshotManager() {
    }

    public static Optional<File> takeScreenshot(Stage stage) {
        if (stage == null || stage.getScene() == null) {
            LOG.error("Не удалось создать скриншот: окно или сцена не определены");
            DialogManager.showError("Не удалось создать скриншот: окно не определено");
            return Optional.empty();
        }

        Optional<File> fileOptional = DialogManager.showSaveScreenshotDialog(stage);
        if (fileOptional.isEmpty()) {
            return Optional.empty();
        }

        return saveScreenshot(stage.getScene(), fileOptional.get());
    }

    private static Optional<File> saveScreenshot(Scene scene, File file) {
        try {
            InputValidator.validateNotNull(scene, "Сцена");
            InputValidator.validateNotNull(file, "Файл");

            String filePath = ensurePngExtension(file.getAbsolutePath());
            File finalFile = new File(filePath);

            PathManager.validatePathForSave(filePath);
            saveImageToFile(scene, finalFile);

            LOG.info("Скриншот успешно сохранен: {}", finalFile.getAbsolutePath());
            return Optional.of(finalFile);
        } catch (Exception e) {
            handleScreenshotError(e);
            return Optional.empty();
        }
    }

    private static String ensurePngExtension(String filePath) {
        InputValidator.validateNotEmpty(filePath, "Путь к файлу");
        return PathManager.ensureExtension(filePath, ".png");
    }

    private static void saveImageToFile(Scene scene, File file) throws IOException {
        WritableImage image = scene.snapshot(null);
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), PNG_FORMAT, file);
    }

    private static void handleScreenshotError(Exception e) {
        LOG.error("Ошибка создания скриншота: {}", e.getMessage(), e);
        DialogManager.showError("Ошибка создания скриншота: " + e.getMessage());
    }
}
