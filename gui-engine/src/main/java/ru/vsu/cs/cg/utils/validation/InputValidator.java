package ru.vsu.cs.cg.utils.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Logger LOG = LoggerFactory.getLogger(InputValidator.class);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    private InputValidator() {
    }

    public static boolean isValidDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return DOUBLE_PATTERN.matcher(value.replace(',', '.')).matches();
    }

    public static double parseDoubleSafe(String value, double defaultValue) {
        if (!isValidDouble(value)) {
            LOG.warn("Некорректное значение double: '{}', используется значение по умолчанию: {}", value, defaultValue);
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга double: '{}'", value, e);
            return defaultValue;
        }
    }


    public static double clamp(double value, double min, double max) {
        if (value < min) {
            LOG.debug("Значение {} ограничено снизу до {}", value, min);
            return min;
        }
        if (value > max) {
            LOG.debug("Значение {} ограничено сверху до {}", value, max);
            return max;
        }
        return value;
    }

    public static void validateNotNull(Object object, String objectName) {
        if (object == null) {
            String message = objectName + " не может быть null";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            String message = fieldName + " не может быть пустым";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }
}
