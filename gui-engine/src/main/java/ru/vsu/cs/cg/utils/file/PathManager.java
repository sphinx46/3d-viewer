package ru.vsu.cs.cg.utils.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.vsu.cs.cg.exception.FileOperationException;
import ru.vsu.cs.cg.exception.ValidationException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;
import ru.vsu.cs.cg.utils.validation.InputValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public final class PathManager {

    private static final Logger LOG = LoggerFactory.getLogger(PathManager.class);
    private static final List<String> SUPPORTED_3D_FORMATS = Arrays.asList(".obj", ".stl", ".fbx", ".3ds");
    private static final List<String> SUPPORTED_IMAGE_FORMATS = Arrays.asList(".png", ".jpg", ".jpeg", ".bmp");
    private static final List<String> SUPPORTED_SCENE_FORMATS = Arrays.asList(".3dscene", ".json");
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();

    private PathManager() {
    }

    public static String normalizePath(String filePath) {
        try {
            if (filePath == null) {
                return "";
            }
            String normalized = filePath.trim().replace("\\", "/");
            LOG.debug("Нормализован путь: '{}' -> '{}'", filePath, normalized);
            return normalized;
        } catch (Exception e) {
            LOG.error("Ошибка нормализации пути '{}': {}", filePath, e.getMessage(), e);
            return filePath != null ? filePath : "";
        }
    }

    public static String ensureExtension(String filePath, String expectedExtension) {
        try {
            InputValidator.validateNotNull(filePath, "Путь к файлу");

            String normalized = normalizePath(filePath);
            if (!normalized.toLowerCase().endsWith(expectedExtension.toLowerCase())) {
                String newPath = normalized + expectedExtension;
                LOG.debug("Добавлено расширение {} к пути: {}", expectedExtension, newPath);
                return newPath;
            }
            return normalized;
        } catch (Exception e) {
            LOG.error("Ошибка добавления расширения {} к пути '{}': {}", expectedExtension, filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, FILE_OPERATION_ERROR);
            throw new FileOperationException(FILE_OPERATION_ERROR + ": " + filePath, e);
        }
    }

    public static String getFileNameWithoutExtension(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                return "";
            }
            String normalized = normalizePath(filePath);
            String fileName = new File(normalized).getName();
            int dotIndex = fileName.lastIndexOf('.');
            return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        } catch (Exception e) {
            LOG.error("Ошибка получения имени файла без расширения '{}': {}", filePath, e.getMessage(), e);
            return "";
        }
    }

    public static String getFileExtension(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                return "";
            }
            String normalized = normalizePath(filePath);
            String fileName = new File(normalized).getName();
            int dotIndex = fileName.lastIndexOf('.');
            return dotIndex > 0 ? fileName.substring(dotIndex).toLowerCase() : "";
        } catch (Exception e) {
            LOG.error("Ошибка получения расширения файла '{}': {}", filePath, e.getMessage(), e);
            return "";
        }
    }

    public static boolean isSupported3DFormat(String filePath) {
        try {
            String extension = getFileExtension(filePath);
            return SUPPORTED_3D_FORMATS.contains(extension);
        } catch (Exception e) {
            LOG.error("Ошибка проверки формата 3D файла '{}': {}", filePath, e.getMessage(), e);
            return false;
        }
    }

    public static boolean isSupportedSceneFormat(String filePath) {
        try {
            String extension = getFileExtension(filePath);
            return SUPPORTED_SCENE_FORMATS.contains(extension);
        } catch (Exception e) {
            LOG.error("Ошибка проверки формата сцены '{}': {}", filePath, e.getMessage(), e);
            return false;
        }
    }

    public static void validatePathForSave(String filePath) {
        try {
            InputValidator.validateNotEmpty(filePath, "Путь к файлу");

            String normalizedPath = normalizePath(filePath);
            validatePathFormat(normalizedPath);
            ensureDirectoryExists(normalizedPath);
            checkWritePermissions(normalizedPath);
            warnIfFileExists(normalizedPath);

            LOG.debug("Путь к файлу успешно валидирован для сохранения: {}", normalizedPath);
        } catch (Exception e) {
            LOG.error("Ошибка валидации пути для сохранения '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, FILE_OPERATION_ERROR);
            throw new FileOperationException(FILE_OPERATION_ERROR + ": " + filePath, e);
        }
    }

    public static void validatePathForRead(String filePath) {
        try {
            InputValidator.validateNotEmpty(filePath, "Путь к файлу");

            String normalizedPath = normalizePath(filePath);
            validatePathFormat(normalizedPath);
            checkFileExists(normalizedPath);
            checkReadPermissions(normalizedPath);

            LOG.debug("Путь к файлу успешно валидирован для чтения: {}", normalizedPath);
        } catch (Exception e) {
            LOG.error("Ошибка валидации пути для чтения '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, FILE_OPERATION_ERROR);
            throw new FileOperationException(FILE_OPERATION_ERROR + ": " + filePath, e);
        }
    }

    private static void validatePathFormat(String filePath) {
        try {
            Paths.get(filePath);
        } catch (InvalidPathException e) {
            String message = FILE_FORMAT_ERROR + ": " + filePath;
            LOG.error(message, e);
            throw new ValidationException(message, e);
        }
    }

    public static void ensureDirectoryExists(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();

            if (parentDir != null && !Files.exists(parentDir)) {
                LOG.info("Создание директории: {}", parentDir.toAbsolutePath());
                try {
                    Files.createDirectories(parentDir);
                } catch (IOException e) {
                    String message = FILE_OPERATION_ERROR + ": Не удалось создать директорию: " + parentDir.toAbsolutePath();
                    LOG.error(message, e);
                    throw new FileOperationException(message, e);
                }
            }
        } catch (Exception e) {
            LOG.error("Ошибка создания директории для пути '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, FILE_OPERATION_ERROR);
            throw new FileOperationException(FILE_OPERATION_ERROR + ": " + filePath, e);
        }
    }

    private static void checkFileExists(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                String message = FILE_NOT_FOUND + ": " + filePath;
                LOG.error(message);
                throw new FileOperationException(message);
            }
        } catch (Exception e) {
            LOG.error("Ошибка проверки существования файла '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, FILE_OPERATION_ERROR);
            throw new FileOperationException(FILE_OPERATION_ERROR + ": " + filePath, e);
        }
    }

    private static void checkWritePermissions(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();

            if (parentDir == null) {
                parentDir = Paths.get(".");
            }

            if (!Files.isWritable(parentDir)) {
                String message = FILE_PERMISSION_ERROR + ": " + parentDir.toAbsolutePath();
                LOG.error(message);
                throw new SecurityException(message);
            }
        } catch (Exception e) {
            LOG.error("Ошибка проверки прав записи для пути '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, FILE_PERMISSION_ERROR);
            throw new SecurityException(FILE_PERMISSION_ERROR + ": " + filePath, e);
        }
    }

    private static void checkReadPermissions(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.canRead()) {
                String message = FILE_PERMISSION_ERROR + ": " + filePath;
                LOG.error(message);
                throw new SecurityException(message);
            }
        } catch (Exception e) {
            LOG.error("Ошибка проверки прав чтения для файла '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, FILE_PERMISSION_ERROR);
            throw new SecurityException(FILE_PERMISSION_ERROR + ": " + filePath, e);
        }
    }

    private static void warnIfFileExists(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                LOG.warn("Файл уже существует и будет перезаписан: {}", filePath);
            }
        } catch (Exception e) {
            LOG.error("Ошибка проверки существования файла '{}': {}", filePath, e.getMessage(), e);
        }
    }
}
