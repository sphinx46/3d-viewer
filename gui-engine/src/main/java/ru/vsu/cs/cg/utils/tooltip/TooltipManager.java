package ru.vsu.cs.cg.utils.tooltip;

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exception.UIException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public final class TooltipManager {

    private static final Logger LOG = LoggerFactory.getLogger(TooltipManager.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();
    private static final Map<String, String> HOTKEY_TOOLTIPS = new ConcurrentHashMap<>();
    private static final double TOOLTIP_DELAY_MS = 500.0;

    static {
        initializeHotkeyTooltips();
    }

    private TooltipManager() {
    }

    private static void initializeHotkeyTooltips() {
        try {
            HOTKEY_TOOLTIPS.put("selectToolButton", "Выбрать объект (ПКМ)");
            HOTKEY_TOOLTIPS.put("moveToolButton", "Режим перемещения (G)");
            HOTKEY_TOOLTIPS.put("rotateToolButton", "Режим вращения (R)");
            HOTKEY_TOOLTIPS.put("scaleToolButton", "Режим масштабирования (S)");
            HOTKEY_TOOLTIPS.put("addObjectButton", "Добавить объект в сцену");
            HOTKEY_TOOLTIPS.put("deleteObjectButton", "Удалить выбранный объект (Delete)");
            HOTKEY_TOOLTIPS.put("duplicateObjectButton", "Дублировать выбранный объект (Ctrl+D)");
            LOG.debug("Инициализированы горячие клавиши для подсказок");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации горячих клавиш для подсказок: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    public static void addHotkeyTooltip(Control control, String buttonId) {
        try {
            String tooltipText = HOTKEY_TOOLTIPS.get(buttonId);
            if (tooltipText != null) {
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setShowDelay(javafx.util.Duration.millis(TOOLTIP_DELAY_MS));
                Tooltip.install(control, tooltip);
                LOG.trace("Добавлена подсказка с горячей клавишей для: {}", buttonId);
            }
        } catch (Exception e) {
            LOG.error("Ошибка добавления подсказки с горячей клавишей для {}: {}", buttonId, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TOOLTIP_ERROR);
            throw new UIException(TOOLTIP_ERROR + ": " + buttonId, e);
        }
    }

    public static void addCustomTooltip(Control control, String text) {
        try {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setShowDelay(javafx.util.Duration.millis(TOOLTIP_DELAY_MS));
            Tooltip.install(control, tooltip);
            LOG.trace("Добавлена пользовательская подсказка: {}", text);
        } catch (Exception e) {
            LOG.error("Ошибка добавления пользовательской подсказки: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TOOLTIP_ERROR);
            throw new UIException(TOOLTIP_ERROR + ": " + text, e);
        }
    }
}
