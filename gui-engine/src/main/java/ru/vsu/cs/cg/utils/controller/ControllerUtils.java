package ru.vsu.cs.cg.utils.controller;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

public final class ControllerUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerUtils.class);

    private ControllerUtils() {
    }

    public static Optional<Stage> getStage(Node node) {
        if (node == null) {
            LOG.warn("Попытка получить Stage из null узла");
            return Optional.empty();
        }

        if (node.getScene() == null) {
            LOG.warn("Узел не имеет сцены");
            return Optional.empty();
        }

        try {
            Stage stage = (Stage) node.getScene().getWindow();
            return Optional.ofNullable(stage);
        } catch (Exception e) {
            LOG.warn("Не удалось получить Stage из сцены узла: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static boolean applyTheme(AnchorPane anchorPane, String themePath) {
        try {
            URL themeUrl = ControllerUtils.class.getResource(themePath);
            if (themeUrl != null) {
                anchorPane.getStylesheets().clear();
                anchorPane.getStylesheets().add(themeUrl.toExternalForm());
                LOG.info("Тема успешно применена: {}", themePath);
                return true;
            } else {
                LOG.error("Тема не найдена по пути: {}", themePath);
                return false;
            }
        } catch (Exception e) {
            LOG.error("Ошибка применения темы '{}': {}", themePath, e.getMessage());
            return false;
        }
    }

    public static boolean openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                LOG.info("URL успешно открыт: {}", url);
                return true;
            } else {
                LOG.error("Рабочий стол не поддерживает открытие URL");
                return false;
            }
        } catch (Exception e) {
            LOG.error("Ошибка открытия URL '{}': {}", url, e.getMessage());
            return false;
        }
    }

    public static String getFileName(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                LOG.warn("Путь к файлу пуст или null");
                return "Неизвестный файл";
            }
            return new File(filePath).getName();
        } catch (Exception e) {
            LOG.error("Ошибка получения имени файла из пути '{}': {}", filePath, e.getMessage());
            return "Ошибка имени файла";
        }
    }
}
