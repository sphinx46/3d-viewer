package ru.vsu.cs.cg.controller.command.impl.window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.window.WindowManager;

public class LayoutDefaultCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(LayoutDefaultCommand.class);

    @Override
    public void execute() {
        LOG.info("Расположение окон по умолчанию");
        WindowManager.arrangeDefault();
    }

    @Override
    public String getName() {
        return "layout_default";
    }

    @Override
    public String getDescription() {
        return "Расположение окон по умолчанию";
    }
}
