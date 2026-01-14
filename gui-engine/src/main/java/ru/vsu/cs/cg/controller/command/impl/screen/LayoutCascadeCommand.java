package ru.vsu.cs.cg.controller.command.impl.window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.window.WindowManager;

public class LayoutCascadeCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(LayoutCascadeCommand.class);

    @Override
    public void execute() {
        LOG.info("Каскадное расположение окон");
        WindowManager.arrangeCascade();
    }

    @Override
    public String getName() {
        return "layout_cascade";
    }

    @Override
    public String getDescription() {
        return "Каскадное расположение окон";
    }
}
