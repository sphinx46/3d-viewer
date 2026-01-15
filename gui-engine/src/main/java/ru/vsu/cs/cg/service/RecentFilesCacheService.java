package ru.vsu.cs.cg.service;

import java.util.List;

public interface RecentFilesCacheService {
    void addFile(String filePath);
    List<String> getRecentFiles();
    void clearCache();
    boolean isEmpty();
}
