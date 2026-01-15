package ru.vsu.cs.cg.utils.tooltip;

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TooltipManager {

    private static final Logger LOG = LoggerFactory.getLogger(TooltipManager.class);
    private static final Map<String, String> HOTKEY_TOOLTIPS = new ConcurrentHashMap<>();
    private static final double TOOLTIP_DELAY_MS = 500.0;

    static {
        initializeHotkeyTooltips();
    }

    private TooltipManager() {
    }

    private static void initializeHotkeyTooltips() {
        HOTKEY_TOOLTIPS.put("moveToolButton", "Инструмент перемещения (U)");
        HOTKEY_TOOLTIPS.put("rotateToolButton", "Инструмент вращения (I)");
        HOTKEY_TOOLTIPS.put("scaleToolButton", "Инструмент масштабирования (O)");

        HOTKEY_TOOLTIPS.put("addObjectButton", "Добавить объект в сцену");
        HOTKEY_TOOLTIPS.put("deleteObjectButton", "Удалить выбранный объект (Delete)");
        HOTKEY_TOOLTIPS.put("duplicateObjectButton", "Дублировать выбранный объект (Ctrl+D)");

        HOTKEY_TOOLTIPS.put("transform_mode_move", "Инструмент перемещения (U)");
        HOTKEY_TOOLTIPS.put("transform_mode_rotate", "Инструмент вращения (I)");
        HOTKEY_TOOLTIPS.put("transform_mode_scale", "Инструмент масштабирования (O)");
    }

    public static void addHotkeyTooltip(Control control, String buttonId) {
        try {
            String tooltipText = HOTKEY_TOOLTIPS.get(buttonId);
            if (tooltipText != null) {
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setShowDelay(javafx.util.Duration.millis(TOOLTIP_DELAY_MS));
                Tooltip.install(control, tooltip);
                LOG.trace("Добавлена подсказка с горячей клавишей для: {}", buttonId);
            } else {
                LOG.warn("Текст подсказки не найден для ID: {}", buttonId);
            }
        } catch (Exception e) {
            LOG.error("Ошибка добавления подсказки с горячей клавишей для {}: {}", buttonId, e.getMessage());
        }
    }

    public static void addCustomTooltip(Control control, String text) {
        try {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setShowDelay(javafx.util.Duration.millis(TOOLTIP_DELAY_MS));
            Tooltip.install(control, tooltip);
            LOG.trace("Добавлена пользовательская подсказка: {}", text);
        } catch (Exception e) {
            LOG.error("Ошибка добавления пользовательской подсказки: {}", e.getMessage());
        }
    }
}
