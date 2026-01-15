package ru.vsu.cs.cg.utils.events;

import javafx.application.Platform;
import java.util.HashSet;
import java.util.Set;

public class RecentFilesUpdateManager {
    private static RecentFilesUpdateManager instance;
    private final Set<Runnable> listeners = new HashSet<>();

    private RecentFilesUpdateManager() {}

    public static RecentFilesUpdateManager getInstance() {
        if (instance == null) {
            instance = new RecentFilesUpdateManager();
        }
        return instance;
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    public void notifyRecentFilesUpdated() {
        Platform.runLater(() -> {
            for (Runnable listener : listeners) {
                listener.run();
            }
        });
    }
}
