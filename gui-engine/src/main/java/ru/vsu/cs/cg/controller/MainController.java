package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.CommandFactory;
import ru.vsu.cs.cg.controller.command.impl.file.FileOpenCommand;
import ru.vsu.cs.cg.controller.enums.TransformationMode;
import ru.vsu.cs.cg.controller.hotkeys.HotkeyManager;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.service.impl.RecentFilesCacheServiceImpl;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.controller.ControllerUtils;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.file.PathManager;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;

import java.util.List;
import java.util.Optional;

public class MainController {
    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @FXML
    private AnchorPane viewerContainer;
    @FXML
    private TransformController transformPanelController;
    @FXML
    private MaterialController materialPanelController;
    @FXML
    private CameraController cameraPanelController;
    @FXML
    private ModificationController modificationPanelController;


    private final SceneController sceneController = new SceneController();
    private HotkeyManager hotkeyManager;
    private final RecentFilesCacheService recentFilesCacheService = new RecentFilesCacheServiceImpl();
    private CommandFactory commandFactory;
    private RenderController renderController;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TreeView<String> sceneTreeView;
    @FXML
    private Button addObjectButton;
    @FXML
    private Button deleteObjectButton;
    @FXML
    private Button duplicateObjectButton;

    @FXML
    private MenuItem menuThemeDark;
    @FXML
    private MenuItem menuThemeLight;
    @FXML
    private MenuItem menuFileOpen;
    @FXML
    private MenuItem menuFileSaveScene;
    @FXML
    private MenuItem menuFileSaveModel;
    @FXML
    private MenuItem menuFileExit;
    @FXML
    private MenuItem menuCreatePlane;
    @FXML
    private MenuItem menuCreateCube;
    @FXML
    private MenuItem menuCreateCylinder;
    @FXML
    private MenuItem menuCreateCapsule;
    @FXML
    private MenuItem menuCreateSphere;
    @FXML
    private MenuItem menuCreateTeapot;
    @FXML
    private MenuItem menuViewNewWindow;
    @FXML
    private MenuItem menuViewFullscreen;
    @FXML
    private MenuItem menuViewScreenshot;
    @FXML
    private MenuItem menuViewDefault;
    @FXML
    private MenuItem menuViewHorizontal;
    @FXML
    private MenuItem menuViewVertical;
    @FXML
    private MenuItem menuViewCascade;
    @FXML
    private CheckMenuItem menuViewGridHelper;
    @FXML
    private CheckMenuItem menuViewCoordinateAxisHelper;
    @FXML
    private MenuItem menuViewCameraHelper;
    @FXML
    private MenuItem menuViewLightHelper;
    @FXML
    private MenuItem menuCameraFront;
    @FXML
    private MenuItem menuCameraTop;
    @FXML
    private MenuItem menuCameraRight;
    @FXML
    private MenuItem menuCameraLeft;
    @FXML
    private MenuItem menuHelpDocumentation;
    @FXML
    private MenuItem menuHelpShortcuts;
    @FXML
    private MenuItem menuHelpBugReport;
    @FXML
    private MenuItem menuHelpAbout;
    @FXML
    private Menu menuRecent;
    @FXML
    private MenuItem menuRecentClear;
    @FXML
    private Button moveToolButton;
    @FXML
    private Button rotateToolButton;
    @FXML
    private Button scaleToolButton;

    @FXML
    private void initialize() {
        initializeTooltips();
        initializeSceneTree();
        initializeMenuActions();
        initializeButtonActions();
        initializeTransformationButtons();
        loadRecentFiles();
        initializeRender();
        cameraPanelController.initialize();
        cameraPanelController.setSceneManager(renderController.getSceneManager());

        initializeDependencies();
        cameraPanelController.initialize();
        cameraPanelController.setSceneManager(renderController.getSceneManager());

        this.renderController.start();
        LOG.info("Главный контроллер инициализирован");
    }


    private void initializeDependencies() {
        if (transformPanelController != null) {
            transformPanelController.setSceneController(sceneController);
            sceneController.setTransformController(transformPanelController);
        }

        if (materialPanelController != null) {
            materialPanelController.setSceneController(sceneController);
            sceneController.setMaterialController(materialPanelController);
        }

        this.sceneController.setRenderController(renderController);
        this.renderController.setSceneController(sceneController);
        sceneController.setMainController(this);
        sceneController.updateUI();
    }

    public void initializeAfterStageSet() {
        try {
            Stage stage = ControllerUtils.getStage(anchorPane).orElseThrow(
                () -> new IllegalStateException("Stage не найден для anchorPane"));

            commandFactory = new CommandFactory(stage, anchorPane, sceneController, recentFilesCacheService);
            hotkeyManager = new HotkeyManager();
            hotkeyManager.setCommandFactory(commandFactory);
            hotkeyManager.registerGlobalHotkeys(anchorPane);

            initializeMenuCheckmarks();
        } catch (Exception e) {
            LOG.error("Ошибка инициализации командной системы: {}", e.getMessage());
        }
    }

    private void initializeRender() {
        if (viewerContainer == null) {
            LOG.error("viewerContainer не найден!");
            return;
        }
        this.renderController = new RenderController(viewerContainer);
        LOG.info("RenderController создан с viewerContainer");
    }

    private void initializeTooltips() {
        TooltipManager.addHotkeyTooltip(addObjectButton, "addObjectButton");
        TooltipManager.addHotkeyTooltip(deleteObjectButton, "deleteObjectButton");
        TooltipManager.addHotkeyTooltip(duplicateObjectButton, "duplicateObjectButton");
    }

    private void initializeButtonActions() {
        addObjectButton.setOnAction(event -> openFileWithFormatCheck());
        deleteObjectButton.setOnAction(event -> executeCommand("object_delete"));
        duplicateObjectButton.setOnAction(event -> executeCommand("object_duplicate"));
    }

    private void openFileWithFormatCheck() {
        if (commandFactory != null) {
            commandFactory.executeCommand("file_open");
        }
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

                    updateCoordinateAxisMenuState();
                }
            }
        );
    }

    private void initializeMenuActions() {
        menuThemeDark.setOnAction(event -> executeCommand("theme_тёмная"));
        menuThemeLight.setOnAction(event -> executeCommand("theme_светлая"));

        menuFileOpen.setOnAction(event -> openFileWithFormatCheck());
        menuFileSaveScene.setOnAction(event -> executeCommand("scene_save"));
        menuFileSaveModel.setOnAction(event -> executeCommand("model_save"));
        menuFileExit.setOnAction(event -> handleExit());

        menuCreatePlane.setOnAction(event -> executeCommand("model_add_plane"));
        menuCreateCube.setOnAction(event -> executeCommand("model_add_cube"));
        menuCreateCylinder.setOnAction(event -> executeCommand("model_add_cylinder"));
        menuCreateCapsule.setOnAction(event -> executeCommand("model_add_capsule"));
        menuCreateSphere.setOnAction(event -> executeCommand("model_add_sphere"));
        menuCreateTeapot.setOnAction(event -> executeCommand("model_add_teapot"));

        menuViewNewWindow.setOnAction(event -> executeCommand("window_new"));
        menuViewFullscreen.setOnAction(event -> executeCommand("fullscreen_toggle"));
        menuViewScreenshot.setOnAction(event -> executeCommand("screenshot_take"));
        menuViewDefault.setOnAction(event -> executeCommand("layout_default"));
        menuViewHorizontal.setOnAction(event -> executeCommand("layout_horizontal"));
        menuViewVertical.setOnAction(event -> executeCommand("layout_vertical"));
        menuViewCascade.setOnAction(event -> executeCommand("layout_cascade"));
        menuViewGridHelper.setOnAction(event -> {
            executeCommand("grid_toggle");
            menuViewGridHelper.setSelected(sceneController.getCurrentScene().isGridVisible());
        });
        menuViewCoordinateAxisHelper.setOnAction(event -> {
            executeCommand("axis_toggle");
            if (sceneController.hasSelectedObject()) {
                boolean axisVisible = sceneController.getSelectedObject().getRenderSettings().isDrawAxisLines();
                menuViewCoordinateAxisHelper.setSelected(axisVisible);
            }
        });
        menuViewCameraHelper.setOnAction(event -> executeCommand("camera_indicators_toggle"));
        menuViewLightHelper.setOnAction(event -> executeCommand("light_indicators_toggle"));

        menuCameraFront.setOnAction(event -> executeCommand("camera_front"));
        menuCameraTop.setOnAction(event -> executeCommand("camera_top"));
        menuCameraRight.setOnAction(event -> executeCommand("camera_right"));
        menuCameraLeft.setOnAction(event -> executeCommand("camera_left"));

        menuHelpDocumentation.setOnAction(event -> executeCommand("url_open_документацию"));
        menuHelpShortcuts.setOnAction(event -> executeCommand("hotkeys_show"));
        menuHelpBugReport.setOnAction(event -> executeCommand("url_open_сообщить_об_ошибке"));
        menuHelpAbout.setOnAction(event -> executeCommand("about_show"));

        menuRecentClear.setOnAction(event -> clearRecentFiles());
    }

    private void executeCommand(String commandName) {
        if (commandFactory != null) {
            commandFactory.executeCommand(commandName);
        } else {
            DialogManager.showError("Система команд не готова");
        }
    }

    private void initializeMenuCheckmarks() {
        Platform.runLater(() -> {
            menuViewGridHelper.setSelected(sceneController.getCurrentScene().isGridVisible());
            menuViewCoordinateAxisHelper.setSelected(sceneController.getSelectedObject().getRenderSettings().isDrawAxisLines());
            updateCoordinateAxisMenuState();
        });
    }

    private void initializeTransformationButtons() {
        moveToolButton.setOnAction(event -> executeCommand("transform_mode_move"));
        rotateToolButton.setOnAction(event -> executeCommand("transform_mode_rotate"));
        scaleToolButton.setOnAction(event -> executeCommand("transform_mode_scale"));

        TooltipManager.addHotkeyTooltip(moveToolButton, "transform_mode_move");
        TooltipManager.addHotkeyTooltip(rotateToolButton, "transform_mode_rotate");
        TooltipManager.addHotkeyTooltip(scaleToolButton, "transform_mode_scale");
    }

    public void updateTransformationButtons(TransformationMode mode) {
        Platform.runLater(() -> {
            moveToolButton.getStyleClass().remove("tool-button-active");
            rotateToolButton.getStyleClass().remove("tool-button-active");
            scaleToolButton.getStyleClass().remove("tool-button-active");

            switch (mode) {
                case MOVE:
                    moveToolButton.getStyleClass().add("tool-button-active");
                    break;
                case ROTATE:
                    rotateToolButton.getStyleClass().add("tool-button-active");
                    break;
                case SCALE:
                    scaleToolButton.getStyleClass().add("tool-button-active");
                    break;
            }
        });
    }

    private void updateCoordinateAxisMenuState() {
        Platform.runLater(() -> {
            if (sceneController.hasSelectedObject()) {
                boolean axisVisible = sceneController.getSelectedObject().getRenderSettings().isDrawAxisLines();
                menuViewCoordinateAxisHelper.setSelected(axisVisible);
                menuViewCoordinateAxisHelper.setDisable(false);
            } else {
                menuViewCoordinateAxisHelper.setSelected(false);
                menuViewCoordinateAxisHelper.setDisable(true);
            }
        });
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
    }

    private void openRecentFile(String filePath) {
        try {
            if (commandFactory == null) {
                DialogManager.showError("Система команд не готова");
                return;
            }

            PathManager.validatePathForRead(filePath);

            boolean isSceneFormat = filePath.toLowerCase().endsWith(".scene") ||
                filePath.toLowerCase().endsWith(".3dscene");

            if (isSceneFormat) {
                FileOpenCommand openCommand =
                    (FileOpenCommand) commandFactory.getCommand("file_open");
                if (openCommand != null) {
                    openCommand.openSceneFile(filePath);
                }
            } else {
                FileOpenCommand openCommand =
                    (FileOpenCommand) commandFactory.getCommand("file_open");
                if (openCommand!= null) {
                    openCommand.openModelFile(filePath);
                }
            }

            recentFilesCacheService.addFile(filePath);
            updateRecentFilesMenu();
        } catch (Exception e) {
            LOG.error("Не удалось загрузить файл: {}", ControllerUtils.getFileName(filePath));
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
        if (renderController != null) {
            renderController.stop();
        }
        Platform.exit();
    }

    public SceneController getSceneController() {
        return sceneController;
    }
}
