package ru.vsu.cs.cg.controller.hotkeys;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.CommandFactory;

import java.util.*;

public class HotkeyManager {

    private static final Logger LOG = LoggerFactory.getLogger(HotkeyManager.class);
    private final Map<KeyCombination, Runnable> hotkeyActions;
    private final boolean isMac;
    private CommandFactory commandFactory;

    public HotkeyManager() {
        this.hotkeyActions = new HashMap<>();
        this.isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        initializeHotkeys();
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        LOG.debug("CommandFactory установлен в HotkeyManager");
    }

    private void initializeHotkeys() {
        registerCommand("Ctrl+N", "scene_new");
        registerCommand("Ctrl+O", "file_open");
        registerCommand("Ctrl+S", "scene_save");
        registerCommand("Ctrl+C", "object_copy");
        registerCommand("Ctrl+V", "object_paste");
        registerCommand("Ctrl+D", "object_duplicate");

        KeyCombination deleteKey = new KeyCodeCombination(KeyCode.DELETE);
        hotkeyActions.put(deleteKey, () -> execute("object_delete"));

        KeyCombination deleteShift = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN);
        hotkeyActions.put(deleteShift, () -> execute("object_delete"));
        registerCommand("U", "transform_mode_move");
        registerCommand("I", "transform_mode_rotate");
        registerCommand("O", "transform_mode_scale");

        registerCommand("G", "grid_toggle");
        registerCommand("X", "axis_toggle");

        registerCommand("Ctrl+P", "screenshot_take");
        registerCommand("F11", "fullscreen_toggle");
        registerCommand("F1", "hotkeys_show");

        registerCommand("Ctrl+1", "camera_front");
        registerCommand("Ctrl+2", "camera_top");
        registerCommand("Ctrl+3", "camera_right");
        registerCommand("Ctrl+4", "camera_left");

        LOG.info("Инициализировано {} горячих клавиш", hotkeyActions.size());
    }

    private void registerCommand(String combo, String commandName) {
        KeyCombination kc = createCombination(combo);
        if (kc != null) {
            hotkeyActions.put(kc, () -> execute(commandName));
        }
    }

    private void execute(String commandName) {
        Platform.runLater(() -> {
            if (commandFactory != null) {
                commandFactory.executeCommand(commandName);
            }
        });
    }

    private KeyCombination createCombination(String combination) {
        try {
            String[] parts = combination.split("\\+");
            List<KeyCombination.Modifier> modifiers = new ArrayList<>();
            KeyCode keyCode = null;

            for (String part : parts) {
                String normalized = part.trim().toUpperCase();

                if (normalized.equals("CTRL") || normalized.equals("CONTROL")) modifiers.add(isMac ? KeyCombination.META_DOWN : KeyCombination.CONTROL_DOWN);
                else if (normalized.equals("SHIFT")) modifiers.add(KeyCombination.SHIFT_DOWN);
                else if (normalized.equals("ALT")) modifiers.add(KeyCombination.ALT_DOWN);
                else if (normalized.equals("META") || normalized.equals("CMD")) modifiers.add(KeyCombination.META_DOWN);
                else if (normalized.matches("[0-9]")) keyCode = KeyCode.valueOf("DIGIT" + normalized);
                else if (normalized.matches("[A-Z]")) keyCode = KeyCode.valueOf(normalized);
                else if (normalized.startsWith("F")) keyCode = KeyCode.valueOf(normalized);
            }

            if (keyCode != null) {
                return new KeyCodeCombination(keyCode, modifiers.toArray(new KeyCombination.Modifier[0]));
            }
        } catch (Exception e) {
            LOG.error("Ошибка создания комбинации '{}': {}", combination, e.getMessage());
        }
        return null;
    }

    public static Map<String, String> getHotkeyDescriptions() {
        Map<String, String> descriptions = new LinkedHashMap<>();

        descriptions.put("Ctrl+N", "Создать новую сцену");
        descriptions.put("Ctrl+O", "Открыть файл");
        descriptions.put("Ctrl+S", "Сохранить сцену");

        descriptions.put("ПКМ", "Вращение камерой (обзор)");
        descriptions.put("Ctrl+C", "Копировать объект");
        descriptions.put("Ctrl+V", "Вставить объект");
        descriptions.put("Ctrl+D", "Дублировать объект");
        descriptions.put("Delete", "Удалить объект");

        descriptions.put("U", "Инструмент: Перемещение");
        descriptions.put("I", "Инструмент: Вращение");
        descriptions.put("O", "Инструмент: Масштабирование");
        descriptions.put("X", "Вкл/Выкл Оси координат");
        descriptions.put("LMB", "Применение трансформаций");

        descriptions.put("Ctrl+1", "Вид спереди");
        descriptions.put("Ctrl+2", "Вид сверху");
        descriptions.put("Ctrl+3", "Вид справа");
        descriptions.put("Ctrl+4", "Вид слева");

        descriptions.put("G", "Вкл/Выкл Сетка");

        descriptions.put("F11", "Полноэкранный режим");
        descriptions.put("Ctrl+P", "Скриншот");
        descriptions.put("F1", "Показать справку");

        descriptions.put("W/S", "Полет Вперед / Назад");
        descriptions.put("A/D", "Полет Влево / Вправо");
        descriptions.put("Space", "Взлет (Вверх)");
        descriptions.put("Shift", "Спуск");
        descriptions.put("M wheel", "Приближение / Отдаление");
        descriptions.put("RMB", "Повороты камеры");

        return descriptions;
    }

    public void handleKeyEvent(KeyEvent event) {
        LOG.debug("Нажата клавиша: {} (код: {})", event.getText(), event.getCode());
        for (Map.Entry<KeyCombination, Runnable> entry : hotkeyActions.entrySet()) {
            if (entry.getKey().match(event)) {
                event.consume();
                entry.getValue().run();
                return;
            }
        }
    }

    public void registerGlobalHotkeys(javafx.scene.Node node) {
        if (node != null) node.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
    }

    public void unregisterGlobalHotkeys(javafx.scene.Node node) {
        if (node != null) node.removeEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
    }
}