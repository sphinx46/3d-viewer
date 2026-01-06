package ru.vsu.cs.cg.utils.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exception.ApplicationException;
import ru.vsu.cs.cg.exception.FileOperationException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public final class CachePersistenceManager {

    private static final Logger LOG = LoggerFactory.getLogger(CachePersistenceManager.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();
    private static final String CACHE_DIR = ".3d-viewer";
    private static final String CACHE_FILE = "recent_files.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CachePersistenceManager() {
    }

    public static List<String> loadRecentFiles() {
        try {
            Path cachePath = getCacheFilePath();

            if (!Files.exists(cachePath)) {
                LOG.debug("Файл кеша не найден: {}", cachePath);
                return Collections.emptyList();
            }

            List<String> files = OBJECT_MAPPER.readValue(cachePath.toFile(), new TypeReference<List<String>>() {});
            LOG.info("Загружено {} недавних файлов из кеша", files.size());
            return files;

        } catch (IOException e) {
            LOG.error("Ошибка загрузки кеша недавних файлов: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, CACHE_OPERATION_ERROR);
            return Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка загрузки кеша недавних файлов: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, CACHE_OPERATION_ERROR);
            return Collections.emptyList();
        }
    }

    public static void saveRecentFiles(List<String> recentFiles) {
        try {
            if (recentFiles == null) {
                LOG.warn("Попытка сохранить null список недавних файлов");
                return;
            }

            Path cacheDir = getCacheDirPath();
            if (!Files.exists(cacheDir)) {
                try {
                    Files.createDirectories(cacheDir);
                    LOG.debug("Создана директория для кеша: {}", cacheDir);
                } catch (IOException e) {
                    String errorMessage = "Ошибка создания директории для кеша: " + cacheDir;
                    LOG.error(errorMessage, e);
                    throw new FileOperationException(FILE_OPERATION_ERROR + ": " + errorMessage, e);
                }
            }

            Path cachePath = getCacheFilePath();
            OBJECT_MAPPER.writeValue(cachePath.toFile(), recentFiles);
            LOG.debug("Сохранено {} файлов в кеш", recentFiles.size());

        } catch (IOException e) {
            String errorMessage = "Ошибка сохранения кеша: " + e.getMessage();
            LOG.error(errorMessage, e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, CACHE_OPERATION_ERROR);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка сохранения кеша: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, CACHE_OPERATION_ERROR);
        }
    }

    private static Path getCacheDirPath() {
        try {
            String userHome = System.getProperty("user.home");
            if (userHome == null || userHome.trim().isEmpty()) {
                LOG.error("Не удалось получить домашнюю директорию пользователя");
                throw new ApplicationException("Не удалось получить домашнюю директорию пользователя");
            }
            return Paths.get(userHome, CACHE_DIR);
        } catch (Exception e) {
            LOG.error("Ошибка получения пути к директории кеша: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, CACHE_OPERATION_ERROR);
            throw new ApplicationException(CACHE_OPERATION_ERROR + ": получение пути к директории кеша", e);
        }
    }

    private static Path getCacheFilePath() {
        try {
            return getCacheDirPath().resolve(CACHE_FILE);
        } catch (Exception e) {
            LOG.error("Ошибка получения пути к файлу кеша: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, CACHE_OPERATION_ERROR);
            throw new ApplicationException(CACHE_OPERATION_ERROR + ": получение пути к файлу кеша", e);
        }
    }
}
