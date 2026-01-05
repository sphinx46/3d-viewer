package ru.vsu.cs.cg;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.utils.StageManager;
import ru.vsu.cs.cg.utils.WindowManager;

import static com.sun.javafx.application.PlatformImpl.exit;

public class App extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage = StageManager.createPrimaryStage();
            primaryStage.setOnCloseRequest(event -> {
                LOG.info("Получен запрос на закрытие приложения");
                exit();
            });

            WindowManager.registerStage(primaryStage);
            primaryStage.show();

            LOG.info("Приложение успешно запущено");
        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    private void handleStartupError(Exception e) {
        LOG.error("Критическая ошибка при запуске приложения", e);
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            LOG.error("Необработанное исключение в main методе", e);
            System.exit(1);
        }
    }
}
