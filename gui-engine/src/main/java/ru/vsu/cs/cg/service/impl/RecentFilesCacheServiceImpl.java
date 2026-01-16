package ru.vsu.cs.cg.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.service.RecentFilesCacheService;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class  RecentFilesCacheServiceImpl implements RecentFilesCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(RecentFilesCacheServiceImpl.class);
    private static final int MAX_RECENT_FILES = 10;

    private final LinkedHashMap<String, Long> cache;
    private final ReentrantReadWriteLock lock;

    public RecentFilesCacheServiceImpl() {
        this.cache = new LinkedHashMap<String, Long>(MAX_RECENT_FILES, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                return size() > MAX_RECENT_FILES;
            }
        };
        this.lock = new ReentrantReadWriteLock();

        LOG.debug("Кеш недавних файлов создан с максимальным размером: {}", MAX_RECENT_FILES);
    }

    @Override
    public void addFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            LOG.warn("Попытка добавить пустой путь в кеш");
            return;
        }

        String normalizedPath = filePath.trim();
        lock.writeLock().lock();
        try {
            cache.put(normalizedPath, System.currentTimeMillis());
            LOG.debug("Файл добавлен в кеш: {}", normalizedPath);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<String> getRecentFiles() {
        lock.readLock().lock();
        try {
            List<String> files = new ArrayList<>(cache.keySet());
            Collections.reverse(files);
            LOG.debug("Получено {} недавних файлов", files.size());
            return files;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clearCache() {
        lock.writeLock().lock();
        try {
            int size = cache.size();
            cache.clear();
            LOG.info("Кеш недавних файлов очищен. Удалено {} записей", size);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return cache.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }
}
