package ru.vsu.cs.cg.controller.hotkeys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.MainController;

import java.util.HashMap;
import java.util.Map;

public class HotkeyManager {

    private static final Logger LOG = LoggerFactory.getLogger(HotkeyManager.class);
    private final MainController mainController;
    private final Map<String, Runnable> hotkeyActions;

    public HotkeyManager(MainController mainController) {
        this.mainController = mainController;
        this.hotkeyActions = new HashMap<>();
        initializeHotkeys();
    }

    private void initializeHotkeys() {
        hotkeyActions.put("Ctrl+C", () -> {
            if (mainController.getSceneController() != null) {
                mainController.getSceneController().copySelectedObject();
            }
        });
        hotkeyActions.put("Ctrl+V", () -> {
            if (mainController.getSceneController() != null) {
                mainController.getSceneController().pasteCopiedObject();
            }
        });
        hotkeyActions.put("Ctrl+D", () -> {
            if (mainController.getSceneController() != null) {
                mainController.getSceneController().duplicateSelectedObject();
            }
        });
        hotkeyActions.put("Delete", () -> {
            if (mainController.getSceneController() != null) {
                mainController.getSceneController().removeSelectedObject();
            }
        });

        LOG.info("Инициализировано {} горячих клавиш", hotkeyActions.size());
    }

    public static Map<String, String> getHotkeyDescriptions() {
        Map<String, String> descriptions = new HashMap<>();

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
        descriptions.put("G", "Режим перемещения (при активном 3D виде)");
        descriptions.put("R", "Режим вращения (при активном 3D виде)");
        descriptions.put("S", "Режим масштабирования (при активном 3D виде)");

        return descriptions;
    }

    public void registerGlobalHotkeys() {
        LOG.info("Регистрация глобальных горячих клавиш");
    }

    public void unregisterGlobalHotkeys() {
        LOG.info("Удаление регистрации глобальных горячих клавиш");
    }
}
