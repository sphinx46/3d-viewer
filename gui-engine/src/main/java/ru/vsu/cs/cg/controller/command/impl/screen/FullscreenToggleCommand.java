package ru.vsu.cs.cg.controller.command.impl.screen;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.window.WindowManager;

public class FullscreenToggleCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(FullscreenToggleCommand.class);

    private final Stage stage;

    public FullscreenToggleCommand(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void execute() {
        try {
            WindowManager.toggleFullscreen(stage);
            LOG.info("Переключен режим полноэкранного отображения");
        } catch (Exception e) {
            LOG.error("Ошибка переключения полноэкранного режима: {}", e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "fullscreen_toggle";
    }

    @Override
    public String getDescription() {
        return "Переключение полноэкранного режима";
    }
}
