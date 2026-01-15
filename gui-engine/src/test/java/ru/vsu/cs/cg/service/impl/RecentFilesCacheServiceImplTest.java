package ru.vsu.cs.cg.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecentFilesCacheServiceImplTest {

    private RecentFilesCacheServiceImpl cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new RecentFilesCacheServiceImpl();
    }

    @Test
    @DisplayName("Добавление файла в кеш должно увеличивать размер списка")
    void addFile_ShouldIncreaseCacheSize() {
        cacheService.addFile("test1.obj");
        cacheService.addFile("test2.obj");

        List<String> files = cacheService.getRecentFiles();
        assertEquals(2, files.size());
    }

    @Test
    @DisplayName("Добавление пустого пути не должно влиять на кеш")
    void addFile_WithEmptyPath_ShouldNotAdd() {
        cacheService.addFile("");
        cacheService.addFile(null);

        assertTrue(cacheService.isEmpty());
    }

    @Test
    @DisplayName("Получение недавних файлов должно возвращать список в обратном порядке")
    void getRecentFiles_ShouldReturnReversedOrder() {
        cacheService.addFile("first.obj");
        cacheService.addFile("second.obj");
        cacheService.addFile("third.obj");

        List<String> files = cacheService.getRecentFiles();
        assertEquals("third.obj", files.get(0));
        assertEquals("second.obj", files.get(1));
        assertEquals("first.obj", files.get(2));
    }

    @Test
    @DisplayName("Очистка кеша должна удалять все файлы")
    void clearCache_ShouldRemoveAllFiles() {
        cacheService.addFile("test1.obj");
        cacheService.addFile("test2.obj");

        cacheService.clearCache();

        assertTrue(cacheService.isEmpty());
    }

    @Test
    @DisplayName("Кеш должен ограничивать количество файлов")
    void cache_ShouldLimitMaxFiles() {
        for (int i = 1; i <= 15; i++) {
            cacheService.addFile("file" + i + ".obj");
        }

        List<String> files = cacheService.getRecentFiles();
        assertTrue(files.size() <= 10);
    }
}
