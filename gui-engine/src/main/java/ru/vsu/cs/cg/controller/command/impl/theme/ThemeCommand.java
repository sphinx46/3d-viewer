package ru.vsu.cs.cg.controller.command.impl.theme;

import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.controller.ControllerUtils;

public class ThemeCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ThemeCommand.class);

    private final AnchorPane anchorPane;
    private final String themePath;
    private final String themeName;

    public ThemeCommand(AnchorPane anchorPane, String themePath, String themeName) {
        this.anchorPane = anchorPane;
        this.themePath = themePath;
        this.themeName = themeName;
    }

    @Override
    public void execute() {
        try {
            ControllerUtils.applyTheme(anchorPane, themePath);
            LOG.info("Тема изменена на: {}", themeName);
        } catch (Exception e) {
            LOG.error("Ошибка применения темы '{}': {}", themeName, e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "theme_" + themeName.toLowerCase();
    }

    @Override
    public String getDescription() {
        return "Смена темы на " + themeName;
    }
}
