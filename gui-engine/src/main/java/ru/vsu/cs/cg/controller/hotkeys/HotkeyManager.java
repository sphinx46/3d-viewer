package ru.vsu.cs.cg.controller.hotkeys;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.MainController;

import java.util.*;

public class HotkeyManager {

    private static final Logger LOG = LoggerFactory.getLogger(HotkeyManager.class);
    private final MainController mainController;
    private final Map<KeyCombination, Runnable> hotkeyActions;
    private boolean isMac;

    public HotkeyManager(MainController mainController) {
        this.mainController = mainController;
        this.hotkeyActions = new HashMap<>();
        this.isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        initializeHotkeys();
    }

    private void initializeHotkeys() {
        KeyCombination ctrlC = createCombination("Ctrl+C");
        hotkeyActions.put(ctrlC, () -> Platform.runLater(() -> {
            mainController.copyObject();
        }));

        KeyCombination ctrlV = createCombination("Ctrl+V");
        hotkeyActions.put(ctrlV, () -> Platform.runLater(() -> {
            mainController.pasteObject();
        }));

        KeyCombination ctrlD = createCombination("Ctrl+D");
        hotkeyActions.put(ctrlD, () -> Platform.runLater(() -> {
            mainController.duplicateObject();
        }));

        KeyCombination delete = createCombination("Delete");
        hotkeyActions.put(delete, () -> Platform.runLater(() -> {
            mainController.deleteObject();
        }));

        KeyCombination ctrlN = createCombination("Ctrl+N");
        hotkeyActions.put(ctrlN, () -> Platform.runLater(() -> {
            mainController.createNewScene();
        }));

        KeyCombination ctrlO = createCombination("Ctrl+O");
        hotkeyActions.put(ctrlO, () -> Platform.runLater(() -> {
            mainController.openSceneWithCheck();
        }));

        KeyCombination ctrlS = createCombination("Ctrl+S");
        hotkeyActions.put(ctrlS, () -> Platform.runLater(() -> {
            mainController.saveScene();
        }));

        KeyCombination ctrlShiftS = createCombination("Ctrl+Shift+S");
        hotkeyActions.put(ctrlShiftS, () -> Platform.runLater(() -> {
            mainController.saveSceneAs();
        }));

        KeyCombination ctrlShiftN = createCombination("Ctrl+Shift+N");
        hotkeyActions.put(ctrlShiftN, () -> Platform.runLater(() -> {
            mainController.createCustomObject();
        }));

        KeyCombination ctrlP = createCombination("Ctrl+P");
        hotkeyActions.put(ctrlP, () -> Platform.runLater(() -> {
            mainController.takeScreenshot();
        }));

        KeyCombination f11 = createCombination("F11");
        hotkeyActions.put(f11, () -> Platform.runLater(() -> {
            mainController.toggleFullscreen();
        }));

        KeyCombination f1 = createCombination("F1");
        hotkeyActions.put(f1, () -> Platform.runLater(() -> {
            mainController.showHotkeysDialog();
        }));

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
        descriptions.put("Ctrl+W", "Закрыть текущее окно");
        descriptions.put("Ctrl+Q", "Выйти из приложения");
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
