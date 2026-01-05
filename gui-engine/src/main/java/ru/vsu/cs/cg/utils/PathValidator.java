package ru.vsu.cs.cg.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathValidator {

    private static final Logger LOG = LoggerFactory.getLogger(PathValidator.class);
    private static final String OBJ_EXTENSION = ".obj";

    private PathValidator() {
    }

    public static void validateSavePath(String filePath) {
        validateNotNullOrEmpty(filePath, "Путь к файлу");

        String normalizedPath = normalizePath(filePath);
        validatePathFormat(normalizedPath);
        ensureObjExtension(normalizedPath);
        ensureDirectoryExists(normalizedPath);
        checkWritePermissions(normalizedPath);
        warnIfFileExists(normalizedPath);

        LOG.debug("Путь к файлу успешно валидирован: {}", normalizedPath);
    }

    public static String normalizePath(String filePath) {
        if (filePath == null) {
            return "";
        }

        return filePath.trim().replace("\\", "/");
    }

    public static void ensureObjExtension(String filePath) {
        String normalized = normalizePath(filePath);

        if (!normalized.toLowerCase().endsWith(OBJ_EXTENSION)) {
            String newPath = normalized + OBJ_EXTENSION;
            LOG.debug("Добавлено расширение .obj к пути: {}", newPath);
        }

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

    private static void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            String message = fieldName + " не может быть пустым";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
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

    public static void checkWritePermissions(String filePath) {
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

    private static void warnIfFileExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            LOG.warn("Файл уже существует и будет перезаписан: {}", filePath);
        }
    }
}
