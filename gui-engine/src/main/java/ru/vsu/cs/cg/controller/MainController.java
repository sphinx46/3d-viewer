package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.command.CommandFactory;
import ru.vsu.cs.cg.controller.command.impl.file.FileOpenCommand;
import ru.vsu.cs.cg.controller.command.impl.scene.JsonSceneCommand;
import ru.vsu.cs.cg.controller.enums.TransformationMode;
import ru.vsu.cs.cg.controller.hotkeys.HotkeyManager;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.service.impl.RecentFilesCacheServiceImpl;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.events.RecentFilesUpdateManager;
import ru.vsu.cs.cg.utils.file.PathManager;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;
import ru.vsu.cs.cg.utils.window.StageManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainController {
    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);
    private static final double RIGHT_PANEL_MIN_WIDTH = 250.0;
    private static final double RIGHT_PANEL_MAX_WIDTH = 500.0;
    private static final double RESIZER_WIDTH = 3.0;

    @FXML private AnchorPane anchorPane;

    @FXML private AnchorPane viewerContainer;
    @FXML private TransformController transformPanelController;
    @FXML private MaterialController materialPanelController;
    @FXML private CameraController cameraPanelController;
    @FXML private ModificationController modificationPanelController;
    @FXML private TreeView<SceneObject> sceneTreeView;
    @FXML private Button addObjectButton;
    @FXML private Button deleteObjectButton;
    @FXML private Button duplicateObjectButton;
    @FXML private MenuItem menuThemeDark;
    @FXML private MenuItem menuThemeLight;
    @FXML private MenuItem menuFileOpen;
    @FXML private MenuItem menuFileSaveScene;
    @FXML private MenuItem menuFileSaveModel;
    @FXML private MenuItem menuImport;
    @FXML private MenuItem menuExport;
    @FXML private MenuItem menuFileExit;
    @FXML private MenuItem menuCreatePlane;
    @FXML private MenuItem menuCreateCube;
    @FXML private MenuItem menuCreateCylinder;
    @FXML private MenuItem menuCreateCapsule;
    @FXML private MenuItem menuCreateSphere;
    @FXML private MenuItem menuCreateTeapot;
    @FXML private MenuItem menuViewNewWindow;
    @FXML private MenuItem menuViewFullscreen;
    @FXML private MenuItem menuViewScreenshot;
    @FXML private MenuItem menuViewDefault;
    @FXML private MenuItem menuViewHorizontal;
    @FXML private MenuItem menuViewVertical;
    @FXML private MenuItem menuViewCascade;
    @FXML private CheckMenuItem menuViewGridHelper;
    @FXML private CheckMenuItem menuViewCoordinateAxisHelper;
    @FXML private MenuItem menuCameraFront;
    @FXML private MenuItem menuCameraTop;
    @FXML private MenuItem menuCameraRight;
    @FXML private MenuItem menuCameraLeft;
    @FXML private MenuItem menuHelpDocumentation;
    @FXML private MenuItem menuHelpShortcuts;
    @FXML private MenuItem menuHelpBugReport;
    @FXML private MenuItem menuHelpAbout;
    @FXML private Menu menuRecent;
    @FXML private MenuItem menuRecentClear;
    @FXML private Button moveToolButton;
    @FXML private Button rotateToolButton;
    @FXML private Button scaleToolButton;

    private final SceneController sceneController = new SceneController();
    private final RecentFilesCacheService recentFilesCacheService = new RecentFilesCacheServiceImpl();
    private HotkeyManager hotkeyManager;
    private CommandFactory commandFactory;
    private RenderController renderController;

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
        initializeDependencies();
        renderController.start();
        RecentFilesUpdateManager.getInstance().addListener(this::updateRecentFilesMenu);
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

        if (modificationPanelController != null) {
            modificationPanelController.setSceneController(sceneController);
            sceneController.setModificationController(modificationPanelController);
        }

        if (renderController != null){
            renderController.setSceneController(sceneController);
            sceneController.setRenderController(renderController);
        }

        if (cameraPanelController != null){
            cameraPanelController.setSceneManager(Objects.requireNonNull(renderController).getSceneManager());
            sceneController.setCameraController(cameraPanelController);
        }

        this.sceneController.setRenderController(renderController);
        this.renderController.setSceneController(sceneController);
        sceneController.setMainController(this);
        sceneController.updateUI();
    }

    public void initializeAfterStageSet() {
        try {
            Stage stage = StageManager.getStage(anchorPane).orElseThrow(
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

        TooltipManager.addHotkeyTooltip(moveToolButton, "transform_mode_move");
        TooltipManager.addHotkeyTooltip(rotateToolButton, "transform_mode_rotate");
        TooltipManager.addHotkeyTooltip(scaleToolButton, "transform_mode_scale");
    }

    private void initializeButtonActions() {
        addObjectButton.setOnAction(event -> executeCommand("file_open"));
        deleteObjectButton.setOnAction(event -> executeCommand("object_delete"));
        duplicateObjectButton.setOnAction(event -> executeCommand("object_duplicate"));
    }

    private void initializeSceneTree() {
        TreeItem<SceneObject> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        sceneTreeView.setRoot(rootItem);
        sceneTreeView.setShowRoot(false);

        sceneTreeView.setCellFactory(tv -> new TreeCell<SceneObject>() {
            private final Button eyeButton = createEyeButton();
            private final HBox container = createContainer();
            private final javafx.scene.control.Label nameLabel = (javafx.scene.control.Label) container.getChildren().get(0);

            @Override
            protected void updateItem(SceneObject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nameLabel.setText(item.getName());
                    updateEyeButtonIcon(item.isVisible());
                    eyeButton.setOnAction(event -> sceneController.toggleObjectVisibility(item));
                    setGraphic(container);
                    setText(null);
                }
            }

            private Button createEyeButton() {
                Button button = new Button();
                button.getStyleClass().add("eye-button");
                button.setPrefSize(18, 18);
                button.setMinSize(18, 18);
                button.setMaxSize(18, 18);
                button.setStyle("-fx-padding: 0; -fx-background-radius: 2;");
                return button;
            }

            private HBox createContainer() {
                HBox hbox = new HBox();
                hbox.setSpacing(8);
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                javafx.scene.control.Label label = new javafx.scene.control.Label();
                label.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(label, Priority.ALWAYS);

                hbox.getChildren().addAll(label, eyeButton);
                return hbox;
            }

            private void updateEyeButtonIcon(boolean visible) {
                String imagePath = visible ?
                    "/static/images/icons/right-menu/eye.png" :
                    "/static/images/icons/right-menu/eye-closed.png";

                try {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(
                        Objects.requireNonNull(getClass().getResourceAsStream(imagePath)),
                        12, 12, true, true
                    );
                    eyeButton.setGraphic(new ImageView(image));
                } catch (Exception e) {
                    LOG.error("Ошибка загрузки иконки глаза: {}", e.getMessage());
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem renameItem = new MenuItem("Переименовать");
        renameItem.setOnAction(event -> handleRenameObject());
        contextMenu.getItems().add(renameItem);

        MenuItem duplicateItem = new MenuItem("Дублировать");
        duplicateItem.setOnAction(event -> executeCommand("object_duplicate"));
        contextMenu.getItems().add(duplicateItem);

        MenuItem deleteItem = new MenuItem("Удалить");
        deleteItem.setOnAction(event -> executeCommand("object_delete"));
        contextMenu.getItems().add(deleteItem);

        contextMenu.getItems().add(new SeparatorMenuItem());

        MenuItem toggleVisibilityItem = new MenuItem("Переключить видимость");
        toggleVisibilityItem.setOnAction(event -> handleToggleVisibility());
        contextMenu.getItems().add(toggleVisibilityItem);

        sceneTreeView.setContextMenu(contextMenu);

        sceneTreeView.setOnContextMenuRequested(event -> {
            TreeItem<SceneObject> selectedItem = sceneTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() != null) {
                contextMenu.show(sceneTreeView, event.getScreenX(), event.getScreenY());
            }
        });

        sceneTreeView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null && newValue.getValue() != null) {
                    sceneController.handleSceneObjectSelection(newValue.getValue().getName());
                }
            }
        );
    }

    private void handleRenameObject() {
        TreeItem<SceneObject> selectedItem = sceneTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            DialogManager.showError("Пожалуйста, выберите объект для переименования.");
            return;
        }

        SceneObject selectedObject = selectedItem.getValue();
        String currentName = selectedObject.getName();

        TextInputDialog dialog = new TextInputDialog(currentName);
        dialog.setTitle("Переименование объекта");
        dialog.setHeaderText("Введите новое имя для объекта");
        dialog.setContentText("Имя:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();

            if (newName.equals(currentName)) {
                return;
            }

            if (sceneController.getCurrentScene().findObjectByName(newName).isPresent()) {
                DialogManager.showError("Объект с именем '" + newName + "' уже существует в сцене.");
                return;
            }

            sceneController.renameSelectedObject(newName);
        }
    }

    private void handleToggleVisibility() {
        TreeItem<SceneObject> selectedItem = sceneTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() != null) {
            SceneObject object = selectedItem.getValue();
            sceneController.toggleObjectVisibility(object);
        }
    }

    private void initializeMenuActions() {
        menuThemeDark.setOnAction(event -> executeCommand("theme_тёмная"));
        menuThemeLight.setOnAction(event -> executeCommand("theme_светлая"));
        menuFileOpen.setOnAction(event -> executeCommand("file_open"));
        menuFileSaveScene.setOnAction(event -> executeCommand("scene_save"));
        menuFileSaveModel.setOnAction(event -> executeCommand("model_save"));
        menuImport.setOnAction(event -> executeCommand("scene_json_import"));
        menuExport.setOnAction(event -> executeCommand("scene_json_export"));
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

        menuViewGridHelper.setOnAction(event -> executeCommand("grid_toggle"));
        menuViewCoordinateAxisHelper.setOnAction(event -> executeCommand("axis_toggle"));

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
            menuViewGridHelper.setSelected(true);
        });
    }

    private void initializeTransformationButtons() {
        moveToolButton.setOnAction(event -> executeCommand("transform_mode_move"));
        rotateToolButton.setOnAction(event -> executeCommand("transform_mode_rotate"));
        scaleToolButton.setOnAction(event -> executeCommand("transform_mode_scale"));
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

    public void updateSceneTree() {
        Platform.runLater(() -> {
            TreeItem<SceneObject> rootItem = new TreeItem<>();
            rootItem.setExpanded(true);

            SceneObject selectedObject = sceneController.getSelectedObject();

            for (SceneObject obj : sceneController.getCurrentScene().getObjects()) {
                TreeItem<SceneObject> item = new TreeItem<>(obj);
                rootItem.getChildren().add(item);

                if (selectedObject != null && obj.getId().equals(selectedObject.getId())) {
                    sceneTreeView.getSelectionModel().select(item);
                }
            }

            sceneTreeView.setRoot(rootItem);
        });
    }

    private void loadRecentFiles() {
        List<String> recentFiles = CachePersistenceManager.loadRecentFiles();
        recentFiles.forEach(recentFilesCacheService::addFile);
        updateRecentFilesMenu();
    }

    private void updateRecentFilesMenu() {
        Platform.runLater(() -> {
            menuRecent.getItems().clear();

            List<String> recentFiles = recentFilesCacheService.getRecentFiles();

            if (recentFiles.isEmpty()) {
                MenuItem emptyItem = new MenuItem("Нет недавних файлов");
                emptyItem.setDisable(true);
                menuRecent.getItems().add(emptyItem);
                menuRecent.getItems().add(new SeparatorMenuItem());
            } else {
                for (String filePath : recentFiles) {
                    String fileName = PathManager.getFileNameWithoutExtension(filePath);
                    MenuItem fileItem = new MenuItem(fileName);
                    fileItem.setOnAction(event -> openRecentFile(filePath));
                    menuRecent.getItems().add(fileItem);
                }
            }

            menuRecent.getItems().add(new SeparatorMenuItem());
            menuRecent.getItems().add(menuRecentClear);
        });
    }

    private void openRecentFile(String filePath) {
        try {
            if (commandFactory == null) {
                DialogManager.showError("Система команд не готова");
                return;
            }

            PathManager.validatePathForRead(filePath);

            if (PathManager.isSupportedSceneFormat(filePath)) {
                FileOpenCommand openCommand = (FileOpenCommand) commandFactory.getCommand("file_open");
                if (openCommand != null) {
                    openCommand.openSceneFile(filePath);
                }
            } else if (PathManager.isSupported3DFormat(filePath)) {
                FileOpenCommand openCommand = (FileOpenCommand) commandFactory.getCommand("file_open");
                if (openCommand != null) {
                    openCommand.openModelFile(filePath);
                }
            } else if (PathManager.isImportOrExportSceneFormat(filePath)) {
                JsonSceneCommand openCommand = (JsonSceneCommand) commandFactory.getCommand("scene_json_import");
                if (openCommand != null) {
                    openCommand.importSceneFile(filePath);
                }
            } else {
                LOG.error("Неподдерживаемый формат файла: {}", PathManager.getFileNameWithoutExtension(filePath));
                DialogManager.showError("Неподдерживаемый формат файла");
            }
        } catch (Exception e) {
            LOG.error("Не удалось загрузить файл: {}", PathManager.getFileNameWithoutExtension(filePath), e);
            DialogManager.showError("Не удалось загрузить файл: " + PathManager.getFileNameWithoutExtension(filePath));
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
            RecentFilesUpdateManager.getInstance().notifyRecentFilesUpdated();
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
        RecentFilesUpdateManager.getInstance().removeListener(this::updateRecentFilesMenu);
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
