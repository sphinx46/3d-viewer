package ru.vsu.cs.cg.controller.command.impl.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

public class AboutShowCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(AboutShowCommand.class);

    @Override
    public void execute() {
        try {
            String aboutText = "3d-viewer\n" +
                "Версия: 0.0.1\n" +
                "Разработчики: sphinx46, Senpaka, Y66dras1ll\n" +
                "Программа для просмотра и редактирования 3D моделей.";
            DialogManager.showInfo("О программе", aboutText);
            LOG.info("Показано окно 'О программе'");
        } catch (Exception e) {
            LOG.error("Ошибка показа окна 'О программе': {}", e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "about_show";
    }

    @Override
    public String getDescription() {
        return "Показать информацию о программе";
    }
}
