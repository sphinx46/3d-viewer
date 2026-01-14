package ru.vsu.cs.cg.controller.command.impl.window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.window.WindowManager;

public class LayoutVerticalCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(LayoutVerticalCommand.class);

    @Override
    public void execute() {
        LOG.info("Вертикальное расположение окон");
        WindowManager.arrangeVertically();
    }

    @Override
    public String getName() {
        return "layout_vertical";
    }

    @Override
    public String getDescription() {
        return "Вертикальное расположение окон";
    }
}
