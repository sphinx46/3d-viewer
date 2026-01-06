package ru.vsu.cs.cg.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Logger LOG = LoggerFactory.getLogger(InputValidator.class);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d+");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("[^\\\\/:*?\"<>|]+");

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

    public static boolean isValidInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return INTEGER_PATTERN.matcher(value).matches();
    }

    public static int parseIntegerSafe(String value, int defaultValue) {
        if (!isValidInteger(value)) {
            LOG.warn("Некорректное значение integer: '{}', используется значение по умолчанию: {}", value, defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга integer: '{}'", value, e);
            return defaultValue;
        }
    }

    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
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

    public static int clamp(int value, int min, int max) {
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

    public static boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        return FILENAME_PATTERN.matcher(filename).matches();
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

    public static void validatePositive(double value, String fieldName) {
        if (value <= 0) {
            String message = fieldName + " должен быть положительным: " + value;
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            String message = fieldName + " не может быть отрицательным: " + value;
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }
}
