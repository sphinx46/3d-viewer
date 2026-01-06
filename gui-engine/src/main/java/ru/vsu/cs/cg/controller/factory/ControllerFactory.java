package ru.vsu.cs.cg.controller.factory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.App;
import ru.vsu.cs.cg.controller.BaseController;
import ru.vsu.cs.cg.controller.MainController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ControllerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ControllerFactory.class);
    private static final Map<String, BaseController> CONTROLLER_CACHE = new HashMap<>();

    private ControllerFactory() {
    }

    public static <T extends BaseController> T createController(String fxmlPath, Class<T> controllerClass) {
        try {
            if (CONTROLLER_CACHE.containsKey(fxmlPath)) {
                return controllerClass.cast(CONTROLLER_CACHE.get(fxmlPath));
            }

            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
            Node node = loader.load();
            T controller = loader.getController();

            if (controller != null) {
                CONTROLLER_CACHE.put(fxmlPath, controller);
                LOG.debug("Контроллер создан и закэширован: {}", fxmlPath);
            }

            return controller;
        } catch (IOException e) {
            LOG.error("Ошибка создания контроллера для {}: {}", fxmlPath, e.getMessage());
            throw new RuntimeException("Не удалось создать контроллер", e);
        }
    }
}
