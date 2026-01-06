package ru.vsu.cs.cg;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;
import ru.vsu.cs.cg.utils.window.StageManager;

public class App extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();

    @Override
    public void start(Stage primaryStage) {
        try {
            EXCEPTION_HANDLER.initialize();

            Stage stage = StageManager.createPrimaryStage();
            stage.show();

            LOG.info("Приложение успешно запущено");
        } catch (Exception e) {
            LOG.error("Критическая ошибка запуска приложения: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.APPLICATION_INIT_ERROR);
            throw new RuntimeException("Не удалось запустить приложение", e);
        }
    }

    @Override
    public void stop() {
        try {
            LOG.info("Завершение работы приложения");
        } catch (Exception e) {
            LOG.error("Ошибка завершения работы приложения: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            LOG.error("Необработанное исключение в main: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            System.exit(1);
        }
    }
}
