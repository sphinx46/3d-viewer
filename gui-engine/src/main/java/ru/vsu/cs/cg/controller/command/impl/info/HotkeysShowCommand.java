package ru.vsu.cs.cg.controller.command.impl.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.Command;
import ru.vsu.cs.cg.controller.hotkeys.HotkeyManager;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

import java.util.Map;

public class HotkeysShowCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(HotkeysShowCommand.class);

    @Override
    public void execute() {
        try {
            StringBuilder hotkeysText = new StringBuilder("Горячие клавиши 3D Viewer:\n\n");
            Map<String, String> descriptions = HotkeyManager.getHotkeyDescriptions();

            hotkeysText.append("Управление сценой:\n");
            hotkeysText.append("  Ctrl+N          - ").append(descriptions.get("Ctrl+N")).append("\n");
            hotkeysText.append("  Ctrl+O          - ").append(descriptions.get("Ctrl+O")).append("\n");
            hotkeysText.append("  Ctrl+S          - ").append(descriptions.get("Ctrl+S")).append("\n");
            hotkeysText.append("  Ctrl+Shift+S    - ").append(descriptions.get("Ctrl+Shift+S")).append("\n");
            hotkeysText.append("  Ctrl+Shift+N    - ").append(descriptions.get("Ctrl+Shift+N")).append("\n");

            hotkeysText.append("\nРабота с объектами:\n");
            hotkeysText.append("  ПКМ          - ").append(descriptions.get("ПКМ")).append("\n");
            hotkeysText.append("  Ctrl+C          - ").append(descriptions.get("Ctrl+C")).append("\n");
            hotkeysText.append("  Ctrl+V          - ").append(descriptions.get("Ctrl+V")).append("\n");
            hotkeysText.append("  Ctrl+D          - ").append(descriptions.get("Ctrl+D")).append("\n");
            hotkeysText.append("  Delete          - ").append(descriptions.get("Delete")).append("\n");

            hotkeysText.append("\nОкно и просмотр:\n");
            hotkeysText.append("  F11             - ").append(descriptions.get("F11")).append("\n");
            hotkeysText.append("  Ctrl+P          - ").append(descriptions.get("Ctrl+P")).append("\n");

            DialogManager.showInfo("Горячие клавиши", hotkeysText.toString());
            LOG.info("Показан диалог горячих клавиш");
        } catch (Exception e) {
            LOG.error("Ошибка показа диалога горячих клавиш: {}", e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "hotkeys_show";
    }

    @Override
    public String getDescription() {
        return "Показать диалог с горячими клавишами";
    }
}
