package ru.vsu.cs.cg.controller.command.impl.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.controller.ControllerUtils;

public class UrlOpenCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(UrlOpenCommand.class);

    private final String url;
    private final String description;

    public UrlOpenCommand(String url, String description) {
        this.url = url;
        this.description = description;
    }

    @Override
    public void execute() {
        try {
            ControllerUtils.openUrl(url);
            LOG.info("Открыт URL: {}", url);
        } catch (Exception e) {
            LOG.error("Ошибка открытия URL '{}': {}", url, e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "url_open_" + description.toLowerCase().replace(" ", "_");
    }

    @Override
    public String getDescription() {
        return description;
    }
}
