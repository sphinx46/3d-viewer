package ru.vsu.cs.cg.utils.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public final class CachePersistenceManager {

    private static final Logger LOG = LoggerFactory.getLogger(CachePersistenceManager.class);
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
            LOG.warn("Ошибка загрузки кеша: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public static void saveRecentFiles(List<String> recentFiles) {
        try {
            Path cacheDir = getCacheDirPath();
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                LOG.debug("Создана директория для кеша: {}", cacheDir);
            }

            Path cachePath = getCacheFilePath();
            OBJECT_MAPPER.writeValue(cachePath.toFile(), recentFiles);
            LOG.debug("Сохранено {} файлов в кеш", recentFiles.size());

        } catch (IOException e) {
            LOG.error("Ошибка сохранения кеша: {}", e.getMessage(), e);
        }
    }

    private static Path getCacheDirPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CACHE_DIR);
    }

    private static Path getCacheFilePath() {
        return getCacheDirPath().resolve(CACHE_FILE);
    }
}
