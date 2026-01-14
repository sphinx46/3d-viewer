package ru.vsu.cs.cg.controller.command;

import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.impl.camera.MenuCreateCameraCommand;
import ru.vsu.cs.cg.controller.command.impl.file.FileOpenCommand;
import ru.vsu.cs.cg.controller.command.impl.info.AboutShowCommand;
import ru.vsu.cs.cg.controller.command.impl.info.HotkeysShowCommand;
import ru.vsu.cs.cg.controller.command.impl.info.UrlOpenCommand;
import ru.vsu.cs.cg.controller.command.impl.model.DefaultModelAddCommand;
import ru.vsu.cs.cg.controller.command.impl.model.ModelSaveCommand;
import ru.vsu.cs.cg.controller.command.impl.object.*;
import ru.vsu.cs.cg.controller.command.impl.scene.JsonSceneCommand;
import ru.vsu.cs.cg.controller.command.impl.transform.TransformationModeCommand;
import ru.vsu.cs.cg.controller.command.impl.visualization.AxisToggleCommand;
import ru.vsu.cs.cg.controller.command.impl.visualization.GridToggleCommand;
import ru.vsu.cs.cg.controller.command.impl.scene.SceneNewCommand;
import ru.vsu.cs.cg.controller.command.impl.scene.SceneSaveCommand;
import ru.vsu.cs.cg.controller.command.impl.screen.FullscreenToggleCommand;
import ru.vsu.cs.cg.controller.command.impl.screen.ScreenshotCommand;
import ru.vsu.cs.cg.controller.command.impl.theme.ThemeCommand;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.enums.TransformationMode;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CommandFactory.class);

    private final Map<String, Command> commandRegistry = new HashMap<>();

    public CommandFactory(Stage stage, AnchorPane anchorPane,
                          SceneController sceneController,
                          RecentFilesCacheService recentFilesService) {
        initializeCommands(stage, anchorPane, sceneController, recentFilesService);
    }

    private void initializeCommands(Stage stage, AnchorPane anchorPane,
                                    SceneController sceneController,
                                    RecentFilesCacheService recentFilesService) {
        registerCommand(new ThemeCommand(anchorPane, "/static/css/theme-dark.css", "Тёмная"));
        registerCommand(new ThemeCommand(anchorPane, "/static/css/theme-light.css", "Светлая"));

        registerCommand(new SceneSaveCommand(stage, sceneController, recentFilesService));
        registerCommand(new FileOpenCommand(stage, sceneController, recentFilesService));
        registerCommand(new SceneNewCommand(sceneController));

        registerCommand(new ObjectDeleteCommand(sceneController));
        registerCommand(new ObjectCopyCommand(sceneController));
        registerCommand(new ObjectPasteCommand(sceneController));
        registerCommand(new ObjectDuplicateCommand(sceneController));

        for (DefaultModelLoader.ModelType modelType : DefaultModelLoader.ModelType.values()) {
            registerCommand(new DefaultModelAddCommand(sceneController, modelType));
        }
        registerCommand(new ModelSaveCommand(stage, sceneController, recentFilesService));

        registerCommand(new GridToggleCommand(sceneController));
        registerCommand(new AxisToggleCommand(sceneController));

        registerCommand(new TransformationModeCommand(sceneController, TransformationMode.MOVE));
        registerCommand(new TransformationModeCommand(sceneController, TransformationMode.ROTATE));
        registerCommand(new TransformationModeCommand(sceneController, TransformationMode.SCALE));

        registerCommand(new ScreenshotCommand(stage));
        registerCommand(new FullscreenToggleCommand(stage));

        registerCommand(new HotkeysShowCommand());
        registerCommand(new AboutShowCommand());

        registerCommand(new ObjectRenameCommand(sceneController));

        registerCommand(new JsonSceneCommand(stage, sceneController, recentFilesService, JsonSceneCommand.Mode.IMPORT));
        registerCommand(new JsonSceneCommand(stage, sceneController, recentFilesService, JsonSceneCommand.Mode.EXPORT));

        registerCommand(new ru.vsu.cs.cg.controller.command.impl.window.WindowNewCommand());
        registerCommand(new ru.vsu.cs.cg.controller.command.impl.window.LayoutDefaultCommand());
        registerCommand(new ru.vsu.cs.cg.controller.command.impl.window.LayoutHorizontalCommand());
        registerCommand(new ru.vsu.cs.cg.controller.command.impl.window.LayoutVerticalCommand());
        registerCommand(new ru.vsu.cs.cg.controller.command.impl.window.LayoutCascadeCommand());

        registerCommand(new MenuCreateCameraCommand(sceneController, new Vector3f(5, 2, 0), "camera_left"));
        registerCommand(new MenuCreateCameraCommand(sceneController, new Vector3f(-5, 2, 0), "camera_right"));
        registerCommand(new MenuCreateCameraCommand(sceneController, new Vector3f(0, 6, 5), "camera_top"));
        registerCommand(new MenuCreateCameraCommand(sceneController, new Vector3f(0, 2, 5), "camera_front"));

        registerCommand(new UrlOpenCommand("https://github.com/sphinx46/3d-viewer", "Открыть документацию"));
        registerCommand(new UrlOpenCommand("https://github.com/sphinx46/3d-viewer/issues/new", "Сообщить об ошибке"));

        LOG.info("Фабрика команд инициализирована с {} командами", commandRegistry.size());
    }

    public void registerCommand(Command command) {
        commandRegistry.put(command.getName(), command);
        LOG.debug("Команда зарегистрирована: {}", command.getName());
    }

    public Command getCommand(String name) {
        return commandRegistry.get(name);
    }

    public void executeCommand(String name) {
        Command command = getCommand(name);
        if (command != null) {
            LOG.info("Выполнение команды: {}", command.getDescription());
            command.execute();
        } else {
            LOG.warn("Команда не найдена: {}", name);
        }
    }
}
