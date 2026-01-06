package ru.vsu.cs.cg.utils.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exception.ValidationException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;


import java.util.regex.Pattern;

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public final class InputValidator {

    private static final Logger LOG = LoggerFactory.getLogger(InputValidator.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d+");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("[^\\\\/:*?\"<>|]+");

    private InputValidator() {
    }

    public static boolean isValidDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return false;
            }
            return DOUBLE_PATTERN.matcher(value.replace(',', '.')).matches();
        } catch (Exception e) {
            LOG.error("Ошибка валидации double значения '{}': {}", value, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return false;
        }
    }

    public static double parseDoubleSafe(String value, double defaultValue) {
        try {
            if (!isValidDouble(value)) {
                LOG.warn("Некорректное значение double: '{}', используется значение по умолчанию: {}", value, defaultValue);
                return defaultValue;
            }
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга double: '{}'", value, e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, INVALID_VALUE);
            return defaultValue;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка парсинга double: '{}'", value, e);
            EXCEPTION_HANDLER.handleException(e);
            return defaultValue;
        }
    }

    public static boolean isValidInteger(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return false;
            }
            return INTEGER_PATTERN.matcher(value).matches();
        } catch (Exception e) {
            LOG.error("Ошибка валидации integer значения '{}': {}", value, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return false;
        }
    }

    public static int parseIntegerSafe(String value, int defaultValue) {
        try {
            if (!isValidInteger(value)) {
                LOG.warn("Некорректное значение integer: '{}', используется значение по умолчанию: {}", value, defaultValue);
                return defaultValue;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга integer: '{}'", value, e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, INVALID_VALUE);
            return defaultValue;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка парсинга integer: '{}'", value, e);
            EXCEPTION_HANDLER.handleException(e);
            return defaultValue;
        }
    }

    public static boolean isInRange(double value, double min, double max) {
        try {
            return value >= min && value <= max;
        } catch (Exception e) {
            LOG.error("Ошибка проверки диапазона для значения {}: {}", value, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return false;
        }
    }

    public static boolean isInRange(int value, int min, int max) {
        try {
            return value >= min && value <= max;
        } catch (Exception e) {
            LOG.error("Ошибка проверки диапазона для значения {}: {}", value, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return false;
        }
    }

    public static double clamp(double value, double min, double max) {
        try {
            if (value < min) {
                LOG.debug("Значение {} ограничено снизу до {}", value, min);
                return min;
            }
            if (value > max) {
                LOG.debug("Значение {} ограничено сверху до {}", value, max);
                return max;
            }
            return value;
        } catch (Exception e) {
            LOG.error("Ошибка ограничения значения {}: {}", value, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return value;
        }
    }

    public static int clamp(int value, int min, int max) {
        try {
            if (value < min) {
                LOG.debug("Значение {} ограничено снизу до {}", value, min);
                return min;
            }
            if (value > max) {
                LOG.debug("Значение {} ограничено сверху до {}", value, max);
                return max;
            }
            return value;
        } catch (Exception e) {
            LOG.error("Ошибка ограничения значения {}: {}", value, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return value;
        }
    }

    public static boolean isValidFilename(String filename) {
        try {
            if (filename == null || filename.trim().isEmpty()) {
                return false;
            }
            return FILENAME_PATTERN.matcher(filename).matches();
        } catch (Exception e) {
            LOG.error("Ошибка валидации имени файла '{}': {}", filename, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return false;
        }
    }

    public static void validateNotNull(Object object, String objectName) {
        try {
            if (object == null) {
                String message = NULL_PARAMETER + ": " + objectName;
                LOG.error(message);
                throw new ValidationException(message);
            }
        } catch (ValidationException e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, VALIDATION_ERROR);
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка валидации not null: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            throw new ValidationException(VALIDATION_ERROR + ": " + objectName, e);
        }
    }

    public static void validateNotEmpty(String value, String fieldName) {
        try {
            if (value == null || value.trim().isEmpty()) {
                String message = EMPTY_PARAMETER + ": " + fieldName;
                LOG.error(message);
                throw new ValidationException(message);
            }
        } catch (ValidationException e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, VALIDATION_ERROR);
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка валидации not empty: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            throw new ValidationException(VALIDATION_ERROR + ": " + fieldName, e);
        }
    }

    public static void validatePositive(double value, String fieldName) {
        try {
            if (value <= 0) {
                String message = INVALID_VALUE + ": " + fieldName + " должен быть положительным: " + value;
                LOG.error(message);
                throw new ValidationException(message);
            }
        } catch (ValidationException e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, VALIDATION_ERROR);
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка валидации positive: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            throw new ValidationException(VALIDATION_ERROR + ": " + fieldName, e);
        }
    }

    public static void validateNonNegative(double value, String fieldName) {
        try {
            if (value < 0) {
                String message = INVALID_VALUE + ": " + fieldName + " не может быть отрицательным: " + value;
                LOG.error(message);
                throw new ValidationException(message);
            }
        } catch (ValidationException e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, VALIDATION_ERROR);
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка валидации non-negative: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            throw new ValidationException(VALIDATION_ERROR + ": " + fieldName, e);
        }
    }
}
