package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.CommandFactory;
import ru.vsu.cs.cg.controller.factory.ControllerFactory;
import ru.vsu.cs.cg.controller.hotkeys.HotkeyManager;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.service.impl.RecentFilesCacheServiceImpl;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.controller.ControllerUtils;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;

import java.util.List;
import java.util.Optional;

import static ru.vsu.cs.cg.utils.file.PathManager.isSupportedSceneFormat;
import static ru.vsu.cs.cg.utils.file.PathManager.validatePathForRead;

public class MainController {
    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    private TransformController transformController;
    private MaterialController materialController;
    private final SceneController sceneController = new SceneController();
    private HotkeyManager hotkeyManager;
    private final RecentFilesCacheService recentFilesCacheService = new RecentFilesCacheServiceImpl();
    private CommandFactory commandFactory;

    @FXML private AnchorPane anchorPane;
    @FXML private TreeView<String> sceneTreeView;
    @FXML private Button addObjectButton;
    @FXML private Button deleteObjectButton;
    @FXML private Button duplicateObjectButton;

    @FXML private MenuItem menuThemeDark;
    @FXML private MenuItem menuThemeLight;
    @FXML private MenuItem menuFileOpen;
    @FXML private MenuItem menuFileSave;
    @FXML private MenuItem menuFileSaveAs;
    @FXML private MenuItem menuFileExit;
    @FXML private MenuItem menuFileNewCustom;
    @FXML private MenuItem menuCreatePlane;
    @FXML private MenuItem menuCreateCube;
    @FXML private MenuItem menuCreateCone;
    @FXML private MenuItem menuCreateCylinder;
    @FXML private MenuItem menuCreateTeapot;
    @FXML private MenuItem menuSceneNew;
    @FXML private MenuItem menuSceneOpen;
    @FXML private MenuItem menuSceneSave;
    @FXML private MenuItem menuSceneSaveAs;
    @FXML private MenuItem menuSceneReset;
    @FXML private MenuItem menuWindowFullscreen;
    @FXML private MenuItem menuWindowScreenshot;
    @FXML private MenuItem menuHelpDocumentation;
    @FXML private MenuItem menuHelpShortcuts;
    @FXML private MenuItem menuHelpBugReport;
    @FXML private MenuItem menuHelpAbout;
    @FXML private Menu menuRecent;
    @FXML private MenuItem menuRecentClear;

    @FXML
    private void initialize() {
        LOG.info("Инициализация главного контроллера");

        initializeControllers();
        initializeTooltips();
        initializeSceneTree();
        initializeMenuActions();
        initializeButtonActions();
        loadRecentFiles();
        initializeDependencies();

        LOG.info("Главный контроллер успешно инициализирован");
    }

    public void initializeAfterStageSet() {
        try {
            Stage stage = ControllerUtils.getStage(anchorPane).orElseThrow(
                () -> new IllegalStateException("Stage не найден для anchorPane"));

            commandFactory = new CommandFactory(stage, anchorPane, sceneController, recentFilesCacheService);
            hotkeyManager = new HotkeyManager();
            hotkeyManager.setCommandFactory(commandFactory);
            hotkeyManager.registerGlobalHotkeys(anchorPane);

            LOG.debug("Командная система инициализирована");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации командной системы: {}", e.getMessage());
        }
    }

    private void initializeControllers() {
        transformController = ControllerFactory.createController("/fxml/transform-panel.fxml", TransformController.class);
        materialController = ControllerFactory.createController("/fxml/material-panel.fxml", MaterialController.class);
    }

    private void initializeDependencies() {
        this.sceneController.setMainController(this);
        this.sceneController.setTransformController(transformController);
        this.sceneController.setMaterialController(materialController);
        LOG.debug("Зависимости контроллеров инициализированы");
    }

    private void initializeTooltips() {
        TooltipManager.addHotkeyTooltip(addObjectButton, "addObjectButton");
        TooltipManager.addHotkeyTooltip(deleteObjectButton, "deleteObjectButton");
        TooltipManager.addHotkeyTooltip(duplicateObjectButton, "duplicateObjectButton");
    }

    private void initializeSceneTree() {
        TreeItem<String> rootItem = new TreeItem<>("Сцена");
        rootItem.setExpanded(true);
        sceneTreeView.setRoot(rootItem);
        sceneTreeView.setShowRoot(false);

        sceneTreeView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    String objectName = newValue.getValue();
                    if (objectName.endsWith(" (скрыт)")) {
                        objectName = objectName.replace(" (скрыт)", "");
                    }
                    sceneController.handleSceneObjectSelection(objectName);
                }
            }
        );
        LOG.debug("Дерево сцены инициализировано");
    }

    private void initializeMenuActions() {
        menuThemeDark.setOnAction(event -> executeCommand("theme_тёмная"));
        menuThemeLight.setOnAction(event -> executeCommand("theme_светлая"));

        menuFileOpen.setOnAction(event -> executeCommand("model_load"));
        menuFileSave.setOnAction(event -> {
            if (sceneController.hasSelectedObject()) {
                executeCommand("model_save");
            } else {
                executeCommand("scene_save");
            }
        });
        menuFileSaveAs.setOnAction(event -> {
            if (sceneController.hasSelectedObject()) {
                executeCommand("model_save");
            } else {
                executeCommand("scene_save");
            }
        });
        menuFileExit.setOnAction(event -> handleExit());
        menuFileNewCustom.setOnAction(event -> executeCommand("custom_object_create"));

        menuCreatePlane.setOnAction(event -> executeCommand("model_add_plane"));
        menuCreateCube.setOnAction(event -> executeCommand("model_add_cube"));
        menuCreateCone.setOnAction(event -> executeCommand("model_add_cone"));
        menuCreateCylinder.setOnAction(event -> executeCommand("model_add_cylinder"));
        menuCreateTeapot.setOnAction(event -> executeCommand("model_add_teapot"));

        menuSceneNew.setOnAction(event -> executeCommand("scene_new"));
        menuSceneOpen.setOnAction(event -> executeCommand("scene_open"));
        menuSceneSave.setOnAction(event -> executeCommand("scene_save"));
        menuSceneSaveAs.setOnAction(event -> executeCommand("scene_save_as"));
        menuSceneReset.setOnAction(event -> executeCommand("scene_reset"));

        menuWindowFullscreen.setOnAction(event -> executeCommand("fullscreen_toggle"));
        menuWindowScreenshot.setOnAction(event -> executeCommand("screenshot_take"));

        menuHelpDocumentation.setOnAction(event -> executeCommand("url_open_документацию"));
        menuHelpShortcuts.setOnAction(event -> executeCommand("hotkeys_show"));
        menuHelpBugReport.setOnAction(event -> executeCommand("url_open_сообщить_об_ошибке"));
        menuHelpAbout.setOnAction(event -> executeCommand("about_show"));

        menuRecentClear.setOnAction(event -> clearRecentFiles());
    }

    private void initializeButtonActions() {
        addObjectButton.setOnAction(event -> executeCommand("model_load"));
        deleteObjectButton.setOnAction(event -> executeCommand("object_delete"));
        duplicateObjectButton.setOnAction(event -> executeCommand("object_duplicate"));
    }

    private void executeCommand(String commandName) {
        if (commandFactory != null) {
            commandFactory.executeCommand(commandName);
        } else {
            LOG.warn("CommandFactory не инициализирован для команды: {}", commandName);
            DialogManager.showError("Система команд не готова");
        }
    }

    public void updateSceneTree() {
        Platform.runLater(() -> {
            TreeItem<String> rootItem = new TreeItem<>("Сцена");
            rootItem.setExpanded(true);

            SceneObject selectedObject = sceneController.getSelectedObject();
            String selectedObjectName = selectedObject != null ? selectedObject.getName() : null;

            sceneController.getCurrentScene().getObjects().forEach(obj -> {
                String displayName = obj.isVisible() ? obj.getName() : obj.getName() + " (скрыт)";
                TreeItem<String> item = new TreeItem<>(displayName);
                rootItem.getChildren().add(item);
            });

            sceneTreeView.setRoot(rootItem);

            if (selectedObjectName != null) {
                for (TreeItem<String> item : rootItem.getChildren()) {
                    String itemValue = item.getValue();
                    if (itemValue.endsWith(" (скрыт)")) {
                        itemValue = itemValue.replace(" (скрыт)", "");
                    }

                    if (itemValue.equals(selectedObjectName)) {
                        sceneTreeView.getSelectionModel().select(item);
                        break;
                    }
                }
            }
            LOG.debug("Дерево сцены обновлено");
        });
    }

    private void loadRecentFiles() {
        List<String> recentFiles = CachePersistenceManager.loadRecentFiles();
        recentFiles.forEach(recentFilesCacheService::addFile);
        updateRecentFilesMenu();
    }

    private void updateRecentFilesMenu() {
        menuRecent.getItems().clear();

        List<String> recentFiles = recentFilesCacheService.getRecentFiles();

        if (recentFiles.isEmpty()) {
            MenuItem emptyItem = new MenuItem("Нет недавних файлов");
            emptyItem.setDisable(true);
            menuRecent.getItems().add(emptyItem);
            menuRecent.getItems().add(new SeparatorMenuItem());
        } else {
            for (String filePath : recentFiles) {
                String fileName = ControllerUtils.getFileName(filePath);
                MenuItem fileItem = new MenuItem(fileName);
                fileItem.setOnAction(event -> openRecentFile(filePath));
                menuRecent.getItems().add(fileItem);
            }
        }

        menuRecent.getItems().add(new SeparatorMenuItem());
        menuRecent.getItems().add(menuRecentClear);

        LOG.debug("Меню недавних файлов обновлено");
    }

    private void openRecentFile(String filePath) {
        try {
            validatePathForRead(filePath);

            boolean isSceneFormat = isSupportedSceneFormat(filePath);
            if (isSceneFormat) {
                openSceneWithCheck(filePath);
            } else {
                sceneController.addModelToScene(filePath);
            }
            recentFilesCacheService.addFile(filePath);
            updateRecentFilesMenu();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки недавнего файла '{}': {}", filePath, e.getMessage());
            DialogManager.showError("Не удалось загрузить файл: " + ControllerUtils.getFileName(filePath));
        }
    }

    private void clearRecentFiles() {
        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Очистка списка",
            "Вы уверены, что хотите очистить список недавних файлов?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            recentFilesCacheService.clearCache();
            CachePersistenceManager.saveRecentFiles(recentFilesCacheService.getRecentFiles());
            updateRecentFilesMenu();
            LOG.info("Список недавних файлов очищен");
        }
    }

    public void handleExit() {
        if (sceneController.hasUnsavedChanges()) {
            Optional<ButtonType> result = DialogManager.showConfirmation(
                "Несохраненные изменения",
                "В сцене есть несохраненные изменения. Вы уверены, что хотите выйти?"
            );

            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        CachePersistenceManager.saveRecentFiles(recentFilesCacheService.getRecentFiles());
        if (hotkeyManager != null) {
            hotkeyManager.unregisterGlobalHotkeys(anchorPane);
        }
        Platform.exit();
    }

    private void openSceneWithCheck(String filePath) {
        if (sceneController.hasUnsavedChanges() && !DialogManager.confirmUnsavedChanges()) {
            return;
        }

        try {
            validatePathForRead(filePath);

            if (!isSupportedSceneFormat(filePath)) {
                DialogManager.showError("Неподдерживаемый формат сцены");
                return;
            }

            sceneController.loadScene(filePath);
            recentFilesCacheService.addFile(filePath);
            updateRecentFilesMenu();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки сцены: {}", e.getMessage());
            DialogManager.showError("Ошибка загрузки сцены: " + e.getMessage());
        }
    }

    public SceneController getSceneController() {
        return sceneController;
    }
}
