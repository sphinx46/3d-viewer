package ru.vsu.cs.cg.controller.command.impl.window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.window.WindowManager;

public class LayoutHorizontalCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(LayoutHorizontalCommand.class);

    @Override
    public void execute() {
        LOG.info("Горизонтальное расположение окон");
        WindowManager.arrangeHorizontally();
    }

    @Override
    public String getName() {
        return "layout_horizontal";
    }

    @Override
    public String getDescription() {
        return "Горизонтальное расположение окон";
    }
}
