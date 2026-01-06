package ru.vsu.cs.cg.controller.hotkeys;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.MainController;
import ru.vsu.cs.cg.exception.ApplicationException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;


import java.util.*;
import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public class HotkeyManager {

    private static final Logger LOG = LoggerFactory.getLogger(HotkeyManager.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();
    private final MainController mainController;
    private final Map<KeyCombination, Runnable> hotkeyActions;
    private final boolean isMac;

    public HotkeyManager(MainController mainController) {
        try {
            this.mainController = mainController;
            this.hotkeyActions = new HashMap<>();
            this.isMac = detectOperatingSystem();
            initializeHotkeys();
        } catch (Exception e) {
            LOG.error("Ошибка создания HotkeyManager: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
            throw new ApplicationException(HOTKEY_ERROR + ": создание HotkeyManager", e);
        }
    }

    private boolean detectOperatingSystem() {
        try {
            String osName = System.getProperty("os.name");
            if (osName == null) {
                LOG.warn("Не удалось определить операционную систему");
                return false;
            }
            return osName.toLowerCase().contains("mac");
        } catch (Exception e) {
            LOG.error("Ошибка определения операционной системы: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return false;
        }
    }

    private void initializeHotkeys() {
        try {
            KeyCombination ctrlC = createCombination("Ctrl+C");
            if (ctrlC != null) hotkeyActions.put(ctrlC, () -> Platform.runLater(this::safeExecuteCopyObject));

            KeyCombination ctrlV = createCombination("Ctrl+V");
            if (ctrlV != null) hotkeyActions.put(ctrlV, () -> Platform.runLater(this::safeExecutePasteObject));

            KeyCombination ctrlD = createCombination("Ctrl+D");
            if (ctrlD != null) hotkeyActions.put(ctrlD, () -> Platform.runLater(this::safeExecuteDuplicateObject));

            KeyCombination delete = createCombination("Delete");
            if (delete != null) hotkeyActions.put(delete, () -> Platform.runLater(this::safeExecuteDeleteObject));

            KeyCombination ctrlN = createCombination("Ctrl+N");
            if (ctrlN != null) hotkeyActions.put(ctrlN, () -> Platform.runLater(this::safeExecuteCreateNewScene));

            KeyCombination ctrlO = createCombination("Ctrl+O");
            if (ctrlO != null) hotkeyActions.put(ctrlO, () -> Platform.runLater(this::safeExecuteOpenSceneWithCheck));

            KeyCombination ctrlS = createCombination("Ctrl+S");
            if (ctrlS != null) hotkeyActions.put(ctrlS, () -> Platform.runLater(this::safeExecuteSaveScene));

            KeyCombination ctrlShiftS = createCombination("Ctrl+Shift+S");
            if (ctrlShiftS != null) hotkeyActions.put(ctrlShiftS, () -> Platform.runLater(this::safeExecuteSaveSceneAs));

            KeyCombination ctrlShiftN = createCombination("Ctrl+Shift+N");
            if (ctrlShiftN != null) hotkeyActions.put(ctrlShiftN, () -> Platform.runLater(this::safeExecuteCreateCustomObject));

            KeyCombination ctrlP = createCombination("Ctrl+P");
            if (ctrlP != null) hotkeyActions.put(ctrlP, () -> Platform.runLater(this::safeExecuteTakeScreenshot));

            KeyCombination f11 = createCombination("F11");
            if (f11 != null) hotkeyActions.put(f11, () -> Platform.runLater(this::safeExecuteToggleFullscreen));

            KeyCombination f1 = createCombination("F1");
            if (f1 != null) hotkeyActions.put(f1, () -> Platform.runLater(this::safeExecuteShowHotkeysDialog));

            LOG.info("Инициализировано {} горячих клавиш для {} ОС", hotkeyActions.size(), isMac ? "macOS" : "Windows/Linux");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации горячих клавиш: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
            throw new ApplicationException(HOTKEY_ERROR + ": инициализация горячих клавиш", e);
        }
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
            LOG.error("Ошибка создания комбинации клавиш '{}': {}", combination, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }

        LOG.warn("Не удалось создать комбинацию клавиш для: {}", combination);
        return null;
    }

    public static Map<String, String> getHotkeyDescriptions() {
        try {
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
        } catch (Exception e) {
            LOG.error("Ошибка получения описаний горячих клавиш: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
            return Collections.emptyMap();
        }
    }

    public void handleKeyEvent(KeyEvent event) {
        try {
            for (Map.Entry<KeyCombination, Runnable> entry : hotkeyActions.entrySet()) {
                if (entry.getKey() != null && entry.getKey().match(event)) {
                    event.consume();
                    entry.getValue().run();
                    return;
                }
            }
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка обработки события клавиатуры: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    public void registerGlobalHotkeys(javafx.scene.Node node) {
        try {
            if (node != null) {
                node.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
                LOG.info("Глобальные горячие клавиши зарегистрированы");
            }
        } catch (Exception e) {
            LOG.error("Ошибка регистрации глобальных горячих клавиш: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
            throw new ApplicationException(HOTKEY_ERROR + ": регистрация глобальных горячих клавиш", e);
        }
    }

    public void unregisterGlobalHotkeys(javafx.scene.Node node) {
        try {
            if (node != null) {
                node.removeEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent);
                LOG.info("Регистрация глобальных горячих клавиш удалена");
            }
        } catch (Exception e) {
            LOG.error("Ошибка удаления регистрации глобальных горячих клавиш: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
            throw new ApplicationException(HOTKEY_ERROR + ": удаление регистрации глобальных горячих клавиш", e);
        }
    }

    private void safeExecuteCopyObject() {
        try {
            mainController.copyObject();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения копирования объекта через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecutePasteObject() {
        try {
            mainController.pasteObject();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения вставки объекта через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteDuplicateObject() {
        try {
            mainController.duplicateObject();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения дублирования объекта через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteDeleteObject() {
        try {
            mainController.deleteObject();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения удаления объекта через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteCreateNewScene() {
        try {
            mainController.createNewScene();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения создания новой сцены через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteOpenSceneWithCheck() {
        try {
            mainController.openSceneWithCheck();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения открытия сцены через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteSaveScene() {
        try {
            mainController.saveScene();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения сохранения сцены через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteSaveSceneAs() {
        try {
            mainController.saveSceneAs();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения сохранения сцены как через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteCreateCustomObject() {
        try {
            mainController.createCustomObject();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения создания пользовательского объекта через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteTakeScreenshot() {
        try {
            mainController.takeScreenshot();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения создания скриншота через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteToggleFullscreen() {
        try {
            mainController.toggleFullscreen();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения переключения полноэкранного режима через горячую клавишу: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }

    private void safeExecuteShowHotkeysDialog() {
        try {
            mainController.showHotkeysDialog();
        } catch (Exception e) {
            LOG.error("Ошибка выполнения показа диалога горячих клавиш: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, HOTKEY_ERROR);
        }
    }
}
