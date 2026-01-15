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

            hotkeysText.append("\nРабота с объектами:\n");
            hotkeysText.append("  ПКМ             - ").append(descriptions.get("ПКМ")).append("\n");
            hotkeysText.append("  Ctrl+C          - ").append(descriptions.get("Ctrl+C")).append("\n");
            hotkeysText.append("  Ctrl+V          - ").append(descriptions.get("Ctrl+V")).append("\n");
            hotkeysText.append("  Ctrl+D          - ").append(descriptions.get("Ctrl+D")).append("\n");
            hotkeysText.append("  Delete          - ").append(descriptions.get("Delete")).append("\n");

            hotkeysText.append("\nТрансформации:\n");
            hotkeysText.append("  U               - ").append(descriptions.get("U")).append("\n");
            hotkeysText.append("  I               - ").append(descriptions.get("I")).append("\n");
            hotkeysText.append("  O               - ").append(descriptions.get("O")).append("\n");

            hotkeysText.append("\nВид камеры:\n");
            hotkeysText.append("  Ctrl+1          - ").append(descriptions.get("Ctrl+1")).append("\n");
            hotkeysText.append("  Ctrl+2          - ").append(descriptions.get("Ctrl+2")).append("\n");
            hotkeysText.append("  Ctrl+3          - ").append(descriptions.get("Ctrl+3")).append("\n");
            hotkeysText.append("  Ctrl+4          - ").append(descriptions.get("Ctrl+4")).append("\n");

            hotkeysText.append("\nВизуализация:\n");
            hotkeysText.append("  G               - ").append(descriptions.get("G")).append("\n");
            hotkeysText.append("  X               - ").append(descriptions.get("X")).append("\n");

            hotkeysText.append("\nОкно и просмотр:\n");
            hotkeysText.append("  F11             - ").append(descriptions.get("F11")).append("\n");
            hotkeysText.append("  Ctrl+P          - ").append(descriptions.get("Ctrl+P")).append("\n");

            hotkeysText.append("\nСправка:\n");
            hotkeysText.append("  F1              - ").append(descriptions.get("F1")).append("\n");

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
