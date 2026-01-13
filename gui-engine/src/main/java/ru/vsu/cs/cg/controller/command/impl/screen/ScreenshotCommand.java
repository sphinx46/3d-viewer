package ru.vsu.cs.cg.controller.command.impl.screen;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.dialog.ScreenshotManager;

import java.io.File;
import java.util.Optional;

public class ScreenshotCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotCommand.class);

    private final Stage stage;

    public ScreenshotCommand(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void execute() {
        try {
            Optional<File> screenshotFile = ScreenshotManager.takeScreenshot(stage);
            if (screenshotFile.isPresent()) {
                File file = screenshotFile.get();
                LOG.info("Скриншот сохранен: {}", file.getAbsolutePath());
                DialogManager.showInfo("Скриншот сохранен", "Скриншот сохранен: " + file.getName());
            }
        } catch (Exception e) {
            LOG.error("Ошибка создания скриншота: {}", e.getMessage());
            DialogManager.showError("Ошибка создания скриншота: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "screenshot_take";
    }

    @Override
    public String getDescription() {
        return "Создание скриншота текущего окна";
    }
}
