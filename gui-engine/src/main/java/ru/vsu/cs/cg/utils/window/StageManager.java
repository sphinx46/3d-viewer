package ru.vsu.cs.cg.utils.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.App;

import java.io.IOException;

public final class StageManager {

    private static final Logger LOG = LoggerFactory.getLogger(StageManager.class);
    private static final String FXML_PATH = "/fxml/main.fxml";
    private static final String WINDOW_TITLE = "3d-viewer";
    private static final int DEFAULT_WIDTH = 1400;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int MIN_WIDTH = 1200;
    private static final int MIN_HEIGHT = 700;

    private StageManager() {
    }

    public static Stage createNewWindow() {
        LOG.info("Создание нового окна приложения");

        try {
            Stage stage = createStage();
            LOG.info("Новое окно создано");
            return stage;
        } catch (IOException e) {
            LOG.error("Ошибка создания окна: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать новое окно", e);
        }
    }

    public static Stage createPrimaryStage() throws IOException {
        return createStage();
    }

    private static Stage createStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(FXML_PATH));
        Parent root = loader.load();
        return createStageFromRoot(root);
    }

    private static Stage createStageFromRoot(Parent root) {
        Stage stage = new Stage();
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        LOG.debug("Окно создано с размером: {}x{}", DEFAULT_WIDTH, DEFAULT_HEIGHT);
        return stage;
    }
}
