package ru.vsu.cs.cg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static final String FXML_PATH = "/gui.fxml";
    private static final String WINDOW_TITLE = "3d-viewer";
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 800;
    private static final int MIN_WIDTH = 1200;
    private static final int MIN_HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            Parent root = loader.load();

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setTitle(WINDOW_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);
            primaryStage.show();
        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    private void handleStartupError(Exception e) {
        System.err.println("Ошибка при запуске приложения:");
        e.printStackTrace();
        System.exit(1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
