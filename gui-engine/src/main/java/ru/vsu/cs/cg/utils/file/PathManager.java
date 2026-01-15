package ru.vsu.cs.cg.utils.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.utils.validation.InputValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class PathManager {

    private static final Logger LOG = LoggerFactory.getLogger(PathManager.class);
    private static final List<String> SUPPORTED_3D_FORMATS = List.of(".obj");
    private static final List<String> SUPPORTED_SCENE_FORMATS = List.of(".3dscene");
    private static final List<String> SUPPORTED_EXPORT_FORMATS = List.of(".json");

    private PathManager() {
    }

    public static String normalizePath(String filePath) {
        if (filePath == null) {
            return "";
        }
        String normalized = filePath.trim().replace("\\", "/");
        LOG.debug("Нормализован путь: '{}' -> '{}'", filePath, normalized);
        return normalized;
    }

    public static String ensureExtension(String filePath, String expectedExtension) {
        InputValidator.validateNotNull(filePath, "Путь к файлу");

        String normalized = normalizePath(filePath);
        if (!normalized.toLowerCase().endsWith(expectedExtension.toLowerCase())) {
            String newPath = normalized + expectedExtension;
            LOG.debug("Добавлено расширение {} к пути: {}", expectedExtension, newPath);
            return newPath;
        }
        return normalized;
    }

    public static String getFileNameWithoutExtension(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "";
        }
        String normalized = normalizePath(filePath);
        String fileName = new File(normalized).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    public static String getFileExtension(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "";
        }
        String normalized = normalizePath(filePath);
        String fileName = new File(normalized).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex).toLowerCase() : "";
    }

    public static boolean isSupported3DFormat(String filePath) {
        String extension = getFileExtension(filePath);
        return SUPPORTED_3D_FORMATS.contains(extension);
    }

    public static boolean isSupportedSceneFormat(String filePath) {
        String extension = getFileExtension(filePath);
        return SUPPORTED_SCENE_FORMATS.contains(extension);
    }

    public static boolean isImportOrExportSceneFormat(String filePath) {
        String extension = getFileExtension(filePath);
        return SUPPORTED_EXPORT_FORMATS.contains(extension);
    }

    public static boolean isSupportedFileFormat(String filePath) {
        return isSupportedSceneFormat(filePath) || isSupported3DFormat(filePath);
    }

    public static void validatePathForSave(String filePath) {
        InputValidator.validateNotEmpty(filePath, "Путь к файлу");

        String normalizedPath = normalizePath(filePath);
        validatePathFormat(normalizedPath);
        ensureDirectoryExists(normalizedPath);
        checkWritePermissions(normalizedPath);
        warnIfFileExists(normalizedPath);

        LOG.debug("Путь к файлу успешно валидирован для сохранения: {}", normalizedPath);
    }

    public static void validatePathForRead(String filePath) {
        InputValidator.validateNotEmpty(filePath, "Путь к файлу");

        String normalizedPath = normalizePath(filePath);
        validatePathFormat(normalizedPath);
        checkFileExists(normalizedPath);
        checkReadPermissions(normalizedPath);

        LOG.debug("Путь к файлу успешно валидирован для чтения: {}", normalizedPath);
    }

    private static void validatePathFormat(String filePath) {
        try {
            Paths.get(filePath);
        } catch (InvalidPathException e) {
            String message = "Некорректный формат пути: " + filePath;
            LOG.error(message);
            throw new IllegalArgumentException(message, e);
        }
    }

    public static void ensureDirectoryExists(String filePath) {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();

        if (parentDir != null && !Files.exists(parentDir)) {
            LOG.info("Создание директории: {}", parentDir.toAbsolutePath());
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                String message = "Не удалось создать директорию: " + parentDir.toAbsolutePath();
                LOG.error(message, e);
                throw new IllegalStateException(message, e);
            }
        }
    }

    private static void checkFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            String message = "Файл не найден: " + filePath;
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    private static void checkWritePermissions(String filePath) {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();

        if (parentDir == null) {
            parentDir = Paths.get(".");
        }

        if (!Files.isWritable(parentDir)) {
            String message = "Нет прав на запись в директорию: " + parentDir.toAbsolutePath();
            LOG.error(message);
            throw new SecurityException(message);
        }
    }

    private static void checkReadPermissions(String filePath) {
        File file = new File(filePath);
        if (!file.canRead()) {
            String message = "Нет прав на чтение файла: " + filePath;
            LOG.error(message);
            throw new SecurityException(message);
        }
    }

    private static void warnIfFileExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            LOG.warn("Файл уже существует и будет перезаписан: {}", filePath);
        }
    }
}
