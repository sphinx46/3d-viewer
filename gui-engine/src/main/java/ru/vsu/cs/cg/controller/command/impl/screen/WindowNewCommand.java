package ru.vsu.cs.cg.controller.command.impl.window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.window.WindowManager;

public class WindowNewCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(WindowNewCommand.class);

    @Override
    public void execute() {
        LOG.info("Создание нового окна");
        WindowManager.createAndShowNewWindow();
    }

    @Override
    public String getName() {
        return "window_new";
    }

    @Override
    public String getDescription() {
        return "Создание нового окна приложения";
    }
}
