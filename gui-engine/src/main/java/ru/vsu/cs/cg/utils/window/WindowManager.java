package ru.vsu.cs.cg.utils.window;

import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

import java.util.ArrayList;
import java.util.List;

public final class WindowManager {

    private static final Logger LOG = LoggerFactory.getLogger(WindowManager.class);
    private static final int CASCADE_OFFSET = 30;

    private static final List<Stage> activeStages = new ArrayList<>();

    private WindowManager() {
    }

    public static void createAndShowNewWindow() {
        Platform.runLater(() -> {
            try {
                Stage stage = StageManager.createNewWindow();
                registerStage(stage);
                arrangeCascade(stage);
                stage.show();

                LOG.info("Новое окно создано и показано. Всего окон: {}", activeStages.size());
                DialogManager.showInfo("Новое окно", "Создано новое окно приложения");
            } catch (Exception e) {
                LOG.error("Ошибка создания окна: {}", e.getMessage(), e);
                DialogManager.showError("Ошибка создания окна: " + e.getMessage());
            }
        });
    }

    public static void toggleFullscreen(Stage stage) {
        if (stage == null) {
            LOG.warn("Не удалось переключить полноэкранный режим: окно не определено");
            return;
        }

        boolean newFullscreenState = !stage.isFullScreen();
        stage.setFullScreen(newFullscreenState);

        if (newFullscreenState) {
            LOG.info("Включен полноэкранный режим");
        } else {
            LOG.info("Выключен полноэкранный режим");
        }
    }

    public static void arrangeCascade() {
        LOG.info("Каскадное расположение окон");
        arrangeWindows((stage, index, screen) -> {
            if (index == 0) {
                stage.setX(50);
                stage.setY(50);
            } else {
                Stage prevStage = activeStages.get(index - 1);
                stage.setX(prevStage.getX() + CASCADE_OFFSET);
                stage.setY(prevStage.getY() + CASCADE_OFFSET);
            }
        });
    }

    public static void arrangeHorizontally() {
        LOG.info("Горизонтальное расположение окон");
        arrangeWindows((stage, index, screen) -> {
            double windowWidth = screen.getVisualBounds().getWidth() / activeStages.size();
            stage.setX(index * windowWidth);
            stage.setY(50);
            stage.setWidth(windowWidth);
            stage.setHeight(screen.getVisualBounds().getHeight() * 0.8);
        });
    }

    public static void arrangeVertically() {
        LOG.info("Вертикальное расположение окон");
        arrangeWindows((stage, index, screen) -> {
            double windowHeight = screen.getVisualBounds().getHeight() / activeStages.size();
            stage.setX(100);
            stage.setY(index * windowHeight);
            stage.setWidth(screen.getVisualBounds().getWidth() * 0.8);
            stage.setHeight(windowHeight);
        });
    }

    public static void arrangeDefault() {
        LOG.info("Расположение окон по умолчанию");
        for (Stage stage : activeStages) {
            stage.setX(100);
            stage.setY(100);
        }
    }

    private static void arrangeWindows(WindowArranger arranger) {
        if (activeStages.isEmpty()) return;

        Screen screen = Screen.getPrimary();
        for (int i = 0; i < activeStages.size(); i++) {
            arranger.arrange(activeStages.get(i), i, screen);
        }
    }

    private static void arrangeCascade(Stage newStage) {
        if (activeStages.isEmpty()) {
            newStage.setX(50);
            newStage.setY(50);
        } else {
            Stage lastStage = activeStages.get(activeStages.size() - 1);
            newStage.setX(lastStage.getX() + CASCADE_OFFSET);
            newStage.setY(lastStage.getY() + CASCADE_OFFSET);
        }
    }

    public static void registerStage(Stage stage) {
        if (!activeStages.contains(stage)) {
            activeStages.add(stage);
            stage.setOnCloseRequest(event -> unregisterStage(stage));
            LOG.debug("Зарегистрировано окно. Всего окон: {}", activeStages.size());
        }
    }

    public static void unregisterStage(Stage stage) {
        if (activeStages.remove(stage)) {
            LOG.debug("Окно удалено. Осталось окон: {}", activeStages.size());
        }
    }

    @FunctionalInterface
    private interface WindowArranger {
        void arrange(Stage stage, int index, Screen screen);
    }
}
