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
        KeyCombination ctrlN = createCombination("Ctrl+N");
        if (ctrlN != null) {
            hotkeyActions.put(ctrlN, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("scene_new");
                }
            }));
        }

        KeyCombination ctrlO = createCombination("Ctrl+O");
        if (ctrlO != null) {
            hotkeyActions.put(ctrlO, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("file_open");
                }
            }));
        }

        KeyCombination ctrlS = createCombination("Ctrl+S");
        if (ctrlS != null) {
            hotkeyActions.put(ctrlS, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("scene_save");
                }
            }));
        }

        KeyCombination ctrlC = createCombination("Ctrl+C");
        if (ctrlC != null) {
            hotkeyActions.put(ctrlC, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("object_copy");
                }
            }));
        }

        KeyCombination ctrlV = createCombination("Ctrl+V");
        if (ctrlV != null) {
            hotkeyActions.put(ctrlV, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("object_paste");
                }
            }));
        }

        KeyCombination ctrlD = createCombination("Ctrl+D");
        if (ctrlD != null) {
            hotkeyActions.put(ctrlD, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("object_duplicate");
                }
            }));
        }

        KeyCombination deleteKey = new KeyCodeCombination(KeyCode.DELETE);
        hotkeyActions.put(deleteKey, () -> Platform.runLater(() -> {
            if (commandFactory != null) {
                commandFactory.executeCommand("object_delete");
            }
        }));

        KeyCombination deleteWithShift = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN);
        hotkeyActions.put(deleteWithShift, () -> Platform.runLater(() -> {
            if (commandFactory != null) {
                commandFactory.executeCommand("object_delete");
            }
        }));

        KeyCombination backspaceKey = new KeyCodeCombination(KeyCode.BACK_SPACE);
        hotkeyActions.put(backspaceKey, () -> Platform.runLater(() -> {
            if (commandFactory != null) {
                commandFactory.executeCommand("object_delete");
            }
        }));

        KeyCombination backspaceWithShift = new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.SHIFT_DOWN);
        hotkeyActions.put(backspaceWithShift, () -> Platform.runLater(() -> {
            if (commandFactory != null) {
                commandFactory.executeCommand("object_delete");
            }
        }));

        if (isMac) {
            KeyCombination cmdDelete = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.META_DOWN);
            hotkeyActions.put(cmdDelete, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("object_delete");
                }
            }));

            KeyCombination cmdBackspace = new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.META_DOWN);
            hotkeyActions.put(cmdBackspace, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("object_delete");
                }
            }));
        }

        KeyCombination w = createCombination("W");
        if (w != null) {
            hotkeyActions.put(w, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("transform_mode_move");
                }
            }));
        }

        KeyCombination e = createCombination("E");
        if (e != null) {
            hotkeyActions.put(e, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("transform_mode_rotate");
                }
            }));
        }

        KeyCombination r = createCombination("R");
        if (r != null) {
            hotkeyActions.put(r, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("transform_mode_scale");
                }
            }));
        }

        KeyCombination ctrlP = createCombination("Ctrl+P");
        if (ctrlP != null) {
            hotkeyActions.put(ctrlP, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("screenshot_take");
                }
            }));
        }

        KeyCombination f11 = createCombination("F11");
        if (f11 != null) {
            hotkeyActions.put(f11, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("fullscreen_toggle");
                }
            }));
        }

        KeyCombination f1 = createCombination("F1");
        if (f1 != null) {
            hotkeyActions.put(f1, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("hotkeys_show");
                }
            }));
        }

        KeyCombination ctrl1 = createCombination("Ctrl+1");
        if (ctrl1 != null) {
            hotkeyActions.put(ctrl1, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("camera_front");
                }
            }));
        }

        KeyCombination ctrl2 = createCombination("Ctrl+2");
        if (ctrl2 != null) {
            hotkeyActions.put(ctrl2, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("camera_top");
                }
            }));
        }

        KeyCombination ctrl3 = createCombination("Ctrl+3");
        if (ctrl3 != null) {
            hotkeyActions.put(ctrl3, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("camera_right");
                }
            }));
        }

        KeyCombination ctrl4 = createCombination("Ctrl+4");
        if (ctrl4 != null) {
            hotkeyActions.put(ctrl4, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("camera_left");
                }
            }));
        }

        KeyCombination g = createCombination("G");
        if (g != null) {
            hotkeyActions.put(g, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("grid_toggle");
                }
            }));
        }

        KeyCombination a = createCombination("A");
        if (a != null) {
            hotkeyActions.put(a, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("axis_toggle");
                }
            }));
        }

        LOG.info("Инициализировано {} горячих клавиш для {} ОС", hotkeyActions.size(), isMac ? "macOS" : "Windows/Linux");
    }

    private KeyCombination createCombination(String combination) {
        try {
            String[] parts = combination.split("\\+");
            List<KeyCombination.Modifier> modifiers = new ArrayList<>();
            KeyCode keyCode = null;

            for (String part : parts) {
                String normalized = part.trim().toUpperCase();

                if (normalized.equals("CTRL") || normalized.equals("CONTROL")) {
                    modifiers.add(isMac ? KeyCombination.META_DOWN : KeyCombination.CONTROL_DOWN);
                } else if (normalized.equals("SHIFT")) {
                    modifiers.add(KeyCombination.SHIFT_DOWN);
                } else if (normalized.equals("ALT")) {
                    modifiers.add(KeyCombination.ALT_DOWN);
                } else if (normalized.equals("META") || normalized.equals("CMD")) {
                    modifiers.add(KeyCombination.META_DOWN);
                } else if (normalized.matches("[A-Z]")) {
                    keyCode = KeyCode.valueOf(normalized);
                } else if (normalized.matches("[0-9]")) {
                    keyCode = KeyCode.valueOf("DIGIT" + normalized);
                } else if (normalized.startsWith("F") && normalized.length() > 1) {
                    try {
                        keyCode = KeyCode.valueOf(normalized);
                    } catch (IllegalArgumentException e) {
                        LOG.warn("Неподдерживаемая клавиша F: {}", normalized);
                    }
                }
            }

            if (keyCode != null && !modifiers.isEmpty()) {
                return new KeyCodeCombination(keyCode, modifiers.toArray(new KeyCombination.Modifier[0]));
            } else if (keyCode != null) {
                return new KeyCodeCombination(keyCode);
            }
        } catch (Exception e) {
            LOG.error("Ошибка создания комбинации клавиш '{}': {}", combination, e.getMessage());
        }

        LOG.warn("Не удалось создать комбинацию клавиш для: {}", combination);
        return null;
    }

    public static Map<String, String> getHotkeyDescriptions() {
        Map<String, String> descriptions = new LinkedHashMap<>();

        descriptions.put("Ctrl+N", "Создать новую сцену");
        descriptions.put("Ctrl+O", "Открыть файл (сцену или модель)");
        descriptions.put("Ctrl+S", "Сохранить сцену");

        descriptions.put("ПКМ", "Контекстное меню для работы с объектом");
        descriptions.put("Ctrl+C", "Копировать выбранный объект");
        descriptions.put("Ctrl+V", "Вставить скопированный объект");
        descriptions.put("Ctrl+D", "Дублировать выбранный объект");
        descriptions.put("Delete", "Удалить выбранный объект");

        descriptions.put("W", "Инструмент перемещения");
        descriptions.put("E", "Инструмент вращения");
        descriptions.put("R", "Инструмент масштабирования");

        descriptions.put("Ctrl+P", "Сделать скриншот");
        descriptions.put("F11", "Переключить полноэкранный режим");

        descriptions.put("Ctrl+1", "Вид спереди");
        descriptions.put("Ctrl+2", "Вид сверху");
        descriptions.put("Ctrl+3", "Вид справа");
        descriptions.put("Ctrl+4", "Вид слева");

        descriptions.put("G", "Переключить фоновую сетку");
        descriptions.put("A", "Переключить координатные оси");

        descriptions.put("F1", "Показать горячие клавиши");

        return descriptions;
    }

    public void handleKeyEvent(KeyEvent event) {
        LOG.debug("Нажата клавиша: {} (код: {})", event.getText(), event.getCode());

        for (Map.Entry<KeyCombination, Runnable> entry : hotkeyActions.entrySet()) {
            if (entry.getKey() != null && entry.getKey().match(event)) {
                LOG.debug("Сработала горячая клавиша: {}", event.getCode());
                event.consume();
                entry.getValue().run();
                return;
            }
        }
    }

    public void registerGlobalHotkeys(javafx.scene.Node node) {
        if (node != null) {
            node.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
            LOG.info("Глобальные горячие клавиши зарегистрированы для Mac: {}", isMac);
        }
    }

    public void unregisterGlobalHotkeys(javafx.scene.Node node) {
        if (node != null) {
            node.removeEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
            LOG.info("Регистрация глобальных горячих клавиш удалена");
        }
    }
}
