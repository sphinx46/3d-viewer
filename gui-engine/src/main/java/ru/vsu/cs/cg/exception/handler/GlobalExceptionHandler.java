package ru.vsu.cs.cg.exception.handler;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static GlobalExceptionHandler instance;

    private GlobalExceptionHandler() {
    }

    public static synchronized GlobalExceptionHandler getInstance() {
        if (instance == null) {
            instance = new GlobalExceptionHandler();
        }
        return instance;
    }

    public void initialize() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> handleException(throwable));

        Platform.runLater(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) ->
                handleException(throwable));
        });

        LOG.info("Глобальный обработчик исключений инициализирован");
    }

    public void handleException(Throwable throwable) {
        try {
            String errorMessage = getErrorMessage(throwable);
            String stackTrace = getStackTrace(throwable);

            LOG.error("Необработанное исключение: {}\n{}", errorMessage, stackTrace);

            Platform.runLater(() ->
                DialogManager.showError("Критическая ошибка: " + errorMessage));

        } catch (Exception e) {
            LOG.error("Ошибка в обработчике исключений: {}", e.getMessage(), e);
        }
    }

    public void handleExceptionWithCustomMessage(Throwable throwable, String customMessage) {
        try {
            String stackTrace = getStackTrace(throwable);

            LOG.error("{}: {}\n{}", customMessage, throwable.getMessage(), stackTrace);

            Platform.runLater(() ->
                DialogManager.showError(customMessage + ": " + throwable.getMessage()));

        } catch (Exception e) {
            LOG.error("Ошибка в обработчике исключений: {}", e.getMessage(), e);
        }
    }

    private String getErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "Неизвестная ошибка";
        }

        String message = throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = throwable.getClass().getSimpleName();
        }

        return message;
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public void handleUIException(Runnable uiTask, String errorContext) {
        try {
            uiTask.run();
        } catch (Exception e) {
            LOG.error("Ошибка UI операции '{}': {}", errorContext, e.getMessage(), e);
            DialogManager.showError("Ошибка " + errorContext + ": " + e.getMessage());
        }
    }
}
