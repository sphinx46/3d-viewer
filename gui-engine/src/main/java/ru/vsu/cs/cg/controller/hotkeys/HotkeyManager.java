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

        KeyCombination delete = createCombination("Delete");
        if (delete != null) {
            hotkeyActions.put(delete, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("object_delete");
                }
            }));
        }

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
                    commandFactory.executeCommand("scene_open");
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

        KeyCombination ctrlShiftS = createCombination("Ctrl+Shift+S");
        if (ctrlShiftS != null) {
            hotkeyActions.put(ctrlShiftS, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("scene_save_as");
                }
            }));
        }

        KeyCombination ctrlShiftN = createCombination("Ctrl+Shift+N");
        if (ctrlShiftN != null) {
            hotkeyActions.put(ctrlShiftN, () -> Platform.runLater(() -> {
                if (commandFactory != null) {
                    commandFactory.executeCommand("custom_object_create");
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
                } else if (normalized.equals("DELETE")) {
                    keyCode = KeyCode.DELETE;
                } else if (normalized.matches("[A-Z]")) {
                    keyCode = KeyCode.valueOf(normalized);
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
        descriptions.put("Ctrl+O", "Открыть сцену");
        descriptions.put("Ctrl+S", "Сохранить сцену");
        descriptions.put("Ctrl+Shift+S", "Сохранить сцену как");
        descriptions.put("Ctrl+Shift+N", "Создать пользовательский объект");
        descriptions.put("Ctrl+C", "Копировать выбранный объект");
        descriptions.put("Ctrl+V", "Вставить скопированный объект");
        descriptions.put("Ctrl+D", "Дублировать выбранный объект");
        descriptions.put("Delete", "Удалить выбранный объект");
        descriptions.put("Ctrl+P", "Сделать скриншот");
        descriptions.put("F11", "Переключить полноэкранный режим");
        descriptions.put("F1", "Открыть документацию");

        return descriptions;
    }

    public void handleKeyEvent(KeyEvent event) {
        for (Map.Entry<KeyCombination, Runnable> entry : hotkeyActions.entrySet()) {
            if (entry.getKey() != null && entry.getKey().match(event)) {
                event.consume();
                entry.getValue().run();
                return;
            }
        }
    }

    public void registerGlobalHotkeys(javafx.scene.Node node) {
        if (node != null) {
            node.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
            LOG.info("Глобальные горячие клавиши зарегистрированы");
        }
    }

    public void unregisterGlobalHotkeys(javafx.scene.Node node) {
        if (node != null) {
            node.removeEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
            LOG.info("Регистрация глобальных горячих клавиш удалена");
        }
    }
}
