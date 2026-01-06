package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.factory.ControllerFactory;
import ru.vsu.cs.cg.controller.hotkeys.HotkeyManager;
import ru.vsu.cs.cg.exception.UIException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.service.impl.RecentFilesCacheServiceImpl;
import ru.vsu.cs.cg.utils.cache.CachePersistenceManager;
import ru.vsu.cs.cg.utils.controller.ControllerUtils;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.dialog.ScreenshotManager;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;
import ru.vsu.cs.cg.utils.window.WindowManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();

    private TransformController transformController;
    private MaterialController materialController;
    private final SceneController sceneController = new SceneController();
    private final HotkeyManager hotkeyManager = new HotkeyManager(this);
    private final RecentFilesCacheService recentFilesCacheService = new RecentFilesCacheServiceImpl();

    @FXML private AnchorPane anchorPane;
    @FXML private TreeView<String> sceneTreeView;
    @FXML private Button addObjectButton;
    @FXML private Button deleteObjectButton;
    @FXML private Button duplicateObjectButton;
    @FXML private Button selectToolButton;
    @FXML private Button moveToolButton;
    @FXML private Button rotateToolButton;
    @FXML private Button scaleToolButton;

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
    @FXML private MenuItem menuWindowNew;
    @FXML private MenuItem menuWindowFullscreen;
    @FXML private MenuItem menuWindowScreenshot;
    @FXML private MenuItem menuWindowDefault;
    @FXML private MenuItem menuWindowHorizontal;
    @FXML private MenuItem menuWindowVertical;
    @FXML private MenuItem menuWindowCascade;
    @FXML private MenuItem menuHelpDocumentation;
    @FXML private MenuItem menuHelpShortcuts;
    @FXML private MenuItem menuHelpBugReport;
    @FXML private MenuItem menuHelpAbout;
    @FXML private Menu menuRecent;
    @FXML private MenuItem menuRecentClear;

    @FXML
    private void initialize() {
        LOG.info("Инициализация главного контроллера");

        try {
            initializeControllers();
            initializeTooltips();
            initializeSceneTree();
            initializeMenuActions();
            initializeButtonActions();
            loadRecentFiles();
            initializeDependencies();
            hotkeyManager.registerGlobalHotkeys(anchorPane);

            LOG.info("Главный контроллер успешно инициализирован");
        } catch (Exception e) {
            String errorMessage = "Ошибка инициализации главного контроллера: " + e.getMessage();
            LOG.error(errorMessage, e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.CONTROLLER_INIT_ERROR);
        }
    }

    private void initializeControllers() {
        try {
            transformController = ControllerFactory.createController("/fxml/transform-panel.fxml", TransformController.class);
            materialController = ControllerFactory.createController("/fxml/material-panel.fxml", MaterialController.class);
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.CONTROLLER_INIT_ERROR);
            throw e;
        }
    }

    private void initializeDependencies() {
        try {
            this.sceneController.setMainController(this);
            this.sceneController.setTransformController(transformController);
            this.sceneController.setMaterialController(materialController);

            LOG.debug("Зависимости контроллеров успешно инициализированы");
        } catch (Exception e) {
            String errorMessage = "Ошибка инициализации зависимостей контроллеров: " + e.getMessage();
            LOG.error(errorMessage, e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.CONTROLLER_INIT_ERROR);
        }
    }

    private void initializeTooltips() {
        EXCEPTION_HANDLER.handleUIException(() -> {
            TooltipManager.addHotkeyTooltip(addObjectButton, "addObjectButton");
            TooltipManager.addHotkeyTooltip(deleteObjectButton, "deleteObjectButton");
            TooltipManager.addHotkeyTooltip(duplicateObjectButton, "duplicateObjectButton");
            TooltipManager.addHotkeyTooltip(selectToolButton, "selectToolButton");
            TooltipManager.addHotkeyTooltip(moveToolButton, "moveToolButton");
            TooltipManager.addHotkeyTooltip(rotateToolButton, "rotateToolButton");
            TooltipManager.addHotkeyTooltip(scaleToolButton, "scaleToolButton");
        }, ru.vsu.cs.cg.utils.constants.MessageConstants.TOOLTIP_ERROR);
    }

    private void initializeSceneTree() {
        try {
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
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.UI_OPERATION_ERROR);
        }
    }

    private void initializeMenuActions() {
        menuThemeDark.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(() ->
            ControllerUtils.applyTheme(anchorPane, "/static/css/theme-dark.css"), "Применение темы"));

        menuThemeLight.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(() ->
            ControllerUtils.applyTheme(anchorPane, "/static/css/theme-light.css"), "Применение темы"));

        menuFileOpen.setOnAction(event -> openModel());
        menuFileSave.setOnAction(event -> saveSelectedModel());
        menuFileSaveAs.setOnAction(event -> saveSelectedModel());
        menuFileExit.setOnAction(event -> handleExit());

        menuCreatePlane.setOnAction(event -> addDefaultModel(DefaultModelLoader.ModelType.PLANE));
        menuCreateCube.setOnAction(event -> addDefaultModel(DefaultModelLoader.ModelType.CUBE));
        menuCreateCone.setOnAction(event -> addDefaultModel(DefaultModelLoader.ModelType.CONE));
        menuCreateCylinder.setOnAction(event -> addDefaultModel(DefaultModelLoader.ModelType.CYLINDER));
        menuCreateTeapot.setOnAction(event -> addDefaultModel(DefaultModelLoader.ModelType.TEAPOT));
        menuFileNewCustom.setOnAction(event -> createCustomObject());

        menuSceneNew.setOnAction(event -> createNewScene());
        menuSceneOpen.setOnAction(event -> openSceneWithCheck());
        menuSceneSave.setOnAction(event -> saveScene());
        menuSceneSaveAs.setOnAction(event -> saveSceneAs());
        menuSceneReset.setOnAction(event -> resetScene());

        menuWindowNew.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(() ->
            WindowManager.createAndShowNewWindow(), "Создание окна"));

        menuWindowFullscreen.setOnAction(event -> toggleFullscreen());
        menuWindowScreenshot.setOnAction(event -> takeScreenshot());

        menuWindowDefault.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(WindowManager::arrangeDefault, "Расположение окон"));

        menuWindowHorizontal.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(WindowManager::arrangeHorizontally, "Расположение окон"));

        menuWindowVertical.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(WindowManager::arrangeVertically, "Расположение окон"));

        menuWindowCascade.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(WindowManager::arrangeCascade, "Расположение окон"));

        menuHelpDocumentation.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(() ->
            ControllerUtils.openUrl("https://github.com/sphinx46/3d-viewer"), "Открытие URL"));

        menuHelpShortcuts.setOnAction(event -> showHotkeysDialog());
        menuHelpBugReport.setOnAction(event -> EXCEPTION_HANDLER.handleUIException(() ->
            ControllerUtils.openUrl("https://github.com/sphinx46/3d-viewer/issues/new"), "Открытие URL"));

        menuHelpAbout.setOnAction(event -> showAboutDialog());

        menuRecentClear.setOnAction(event -> clearRecentFiles());
    }

    private void initializeButtonActions() {
        addObjectButton.setOnAction(event -> openModel());
        deleteObjectButton.setOnAction(event -> deleteObject());
        duplicateObjectButton.setOnAction(event -> duplicateObject());
    }

    public void updateSceneTree() {
        Platform.runLater(() -> {
            EXCEPTION_HANDLER.handleUIException(() -> {
                try {
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

                    LOG.debug("Дерево сцены обновлено. Объектов: {}", rootItem.getChildren().size());
                } catch (Exception e) {
                    String errorMessage = "Ошибка обновления дерева сцены: " + e.getMessage();
                    LOG.error(errorMessage, e);
                    throw new UIException(
                        ru.vsu.cs.cg.utils.constants.MessageConstants.UI_UPDATE_ERROR, e);
                }
            }, ru.vsu.cs.cg.utils.constants.MessageConstants.UI_UPDATE_ERROR);
        });
    }

    private void loadRecentFiles() {
        try {
            List<String> recentFiles = CachePersistenceManager.loadRecentFiles();
            recentFiles.forEach(recentFilesCacheService::addFile);
            LOG.info("Загружено {} недавних файлов из кеша", recentFiles.size());
            updateRecentFilesMenu();
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.CACHE_OPERATION_ERROR);
        }
    }

    private void updateRecentFilesMenu() {
        EXCEPTION_HANDLER.handleUIException(() -> {
            try {
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

                LOG.debug("Меню недавних файлов обновлено: {} файлов", recentFiles.size());
            } catch (Exception e) {
                String errorMessage = "Ошибка обновления меню недавних файлов: " + e.getMessage();
                LOG.error(errorMessage, e);
                throw new UIException(
                    ru.vsu.cs.cg.utils.constants.MessageConstants.UI_UPDATE_ERROR, e);
            }
        }, ru.vsu.cs.cg.utils.constants.MessageConstants.UI_UPDATE_ERROR);
    }

    private void openRecentFile(String filePath) {
        try {
            boolean isSceneFormat = ControllerUtils.isSceneFormat(filePath);
            if (isSceneFormat) {
                openSceneWithCheck(filePath);
            } else {
                sceneController.addModelToScene(filePath);
                DialogManager.showSuccess("Модель добавлена: " + ControllerUtils.getFileName(filePath));
            }
            recentFilesCacheService.addFile(filePath);
            updateRecentFilesMenu();
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.FILE_OPERATION_ERROR);
        }
    }

    public void showHotkeysDialog() {
        try {
            StringBuilder hotkeysText = new StringBuilder("Горячие клавиши 3D Viewer:\n\n");
            Map<String, String> descriptions = HotkeyManager.getHotkeyDescriptions();

            hotkeysText.append("Управление сценой:\n");
            hotkeysText.append("  Ctrl+N          - ").append(descriptions.get("Ctrl+N")).append("\n");
            hotkeysText.append("  Ctrl+O          - ").append(descriptions.get("Ctrl+O")).append("\n");
            hotkeysText.append("  Ctrl+S          - ").append(descriptions.get("Ctrl+S")).append("\n");
            hotkeysText.append("  Ctrl+Shift+S    - ").append(descriptions.get("Ctrl+Shift+S")).append("\n");
            hotkeysText.append("  Ctrl+Shift+N    - ").append(descriptions.get("Ctrl+Shift+N")).append("\n");

            hotkeysText.append("\nРабота с объектами:\n");
            hotkeysText.append("  Ctrl+C          - ").append(descriptions.get("Ctrl+C")).append("\n");
            hotkeysText.append("  Ctrl+V          - ").append(descriptions.get("Ctrl+V")).append("\n");
            hotkeysText.append("  Ctrl+D          - ").append(descriptions.get("Ctrl+D")).append("\n");
            hotkeysText.append("  Delete          - ").append(descriptions.get("Delete")).append("\n");

            hotkeysText.append("\nОкно и просмотр:\n");
            hotkeysText.append("  F11             - ").append(descriptions.get("F11")).append("\n");
            hotkeysText.append("  Ctrl+P          - ").append(descriptions.get("Ctrl+P")).append("\n");

            DialogManager.showInfo("Горячие клавиши", hotkeysText.toString());
            LOG.info("Показан диалог горячих клавиш");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.DIALOG_ERROR);
        }
    }

    private void showAboutDialog() {
        try {
            String aboutText = "3d-viewer\n" +
                "Версия: 0.0.1\n" +
                "Разработчики: sphinx46, Senpaka, Y66dras1ll\n" +
                "Программа для просмотра и редактирования 3D моделей.";
            DialogManager.showInfo("О программе", aboutText);
            LOG.info("Показано окно 'О программе'");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.DIALOG_ERROR);
        }
    }

    private void clearRecentFiles() {
        try {
            Optional<ButtonType> result = DialogManager.showConfirmation(
                "Очистка списка",
                "Вы уверены, что хотите очистить список недавних файлов?"
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                recentFilesCacheService.clearCache();
                CachePersistenceManager.saveRecentFiles(recentFilesCacheService.getRecentFiles());
                updateRecentFilesMenu();
                LOG.info("Список недавних файлов очищен пользователем");
            }
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.CACHE_OPERATION_ERROR);
        }
    }

    private void handleExit() {
        try {
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
            hotkeyManager.unregisterGlobalHotkeys(anchorPane);
            Platform.exit();
            LOG.info("Пользователь завершил работу приложения");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.APPLICATION_EXIT_ERROR);
            Platform.exit();
        }
    }

    private void openModel() {
        ControllerUtils.getStage(anchorPane).flatMap(DialogManager::showOpenModelDialog).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();
                sceneController.addModelToScene(filePath);
                recentFilesCacheService.addFile(filePath);
                updateRecentFilesMenu();
                DialogManager.showSuccess("Модель добавлена: " + file.getName());
            } catch (Exception e) {
                EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                    ru.vsu.cs.cg.utils.constants.MessageConstants.MODEL_LOAD_ERROR);
            }
        });
    }

    public void createCustomObject() {
        try {
            sceneController.createCustomObject();
            DialogManager.showInfo("Пользовательский объект", "Создан новый объект для редактирования");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.MODEL_LOAD_ERROR);
        }
    }

    private void addDefaultModel(DefaultModelLoader.ModelType modelType) {
        try {
            sceneController.addDefaultModelToScene(modelType);
            DialogManager.showSuccess("Модель '" + modelType.getDisplayName() + "' добавлена");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.MODEL_LOAD_ERROR);
        }
    }

    private void saveSelectedModel() {
        if (!sceneController.hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        ControllerUtils.getStage(anchorPane).flatMap(DialogManager::showSaveModelDialog).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();
                sceneController.saveSelectedModelToFile(filePath);
                recentFilesCacheService.addFile(filePath);
                updateRecentFilesMenu();
                DialogManager.showSuccess("Модель сохранена: " + file.getName());
            } catch (Exception e) {
                EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                    ru.vsu.cs.cg.utils.constants.MessageConstants.OBJECT_SAVE_ERROR);
            }
        });
    }

    public void createNewScene() {
        try {
            if (sceneController.hasUnsavedChanges()) {
                Optional<ButtonType> result = DialogManager.showConfirmation(
                    "Несохраненные изменения",
                    "В текущей сцене есть несохраненные изменения. Создать новую сцену?"
                );

                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return;
                }
            }

            sceneController.createNewScene();
            DialogManager.showInfo("Новая сцена", "Создана новая сцена");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.SCENE_LOAD_ERROR);
        }
    }

    public void openSceneWithCheck() {
        try {
            if (sceneController.hasUnsavedChanges()) {
                Optional<ButtonType> result = DialogManager.showConfirmation(
                    "Несохраненные изменения",
                    "В текущей сцене есть несохраненные изменения. Открыть новую сцену?"
                );

                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return;
                }
            }

            openScene();
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.SCENE_LOAD_ERROR);
        }
    }

    private void openSceneWithCheck(String filePath) {
        try {
            if (sceneController.hasUnsavedChanges()) {
                Optional<ButtonType> result = DialogManager.showConfirmation(
                    "Несохраненные изменения",
                    "В текущей сцене есть несохраненные изменения. Загрузить сцену из файла?"
                );

                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return;
                }
            }

            sceneController.loadScene(filePath);
            DialogManager.showSuccess("Сцена загружена: " + ControllerUtils.getFileName(filePath));
            recentFilesCacheService.addFile(filePath);
            updateRecentFilesMenu();
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.SCENE_LOAD_ERROR);
        }
    }

    private void openScene() {
        ControllerUtils.getStage(anchorPane).flatMap(DialogManager::showOpenSceneDialog).ifPresent(file -> {
            openSceneWithCheck(file.getAbsolutePath());
        });
    }

    public void saveScene() {
        try {
            if (sceneController.getCurrentSceneFilePath() != null) {
                sceneController.saveScene(sceneController.getCurrentSceneFilePath());
                DialogManager.showSuccess("Сцена сохранена");
            } else {
                saveSceneAs();
            }
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.SCENE_SAVE_ERROR);
        }
    }

    public void saveSceneAs() {
        ControllerUtils.getStage(anchorPane).flatMap(stage ->
            DialogManager.showSaveSceneDialog(stage, sceneController.getCurrentScene().getName())).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();
                sceneController.saveScene(filePath);
                recentFilesCacheService.addFile(filePath);
                updateRecentFilesMenu();
                DialogManager.showSuccess("Сцена сохранена: " + file.getName());
            } catch (Exception e) {
                EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                    ru.vsu.cs.cg.utils.constants.MessageConstants.SCENE_SAVE_ERROR);
            }
        });
    }

    private void resetScene() {
        try {
            if (sceneController.hasUnsavedChanges()) {
                Optional<ButtonType> result = DialogManager.showConfirmation(
                    "Сброс сцены",
                    "Сбросить все изменения в сцене? Несохраненные изменения будут потеряны."
                );

                if (result.isEmpty() || result.get() != ButtonType.OK) return;
            }

            sceneController.createNewScene();
            DialogManager.showInfo("Сброс сцены", "Сцена сброшена до начального состояния");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.SCENE_LOAD_ERROR);
        }
    }

    public void copyObject() {
        try {
            if (!sceneController.hasSelectedObject()) {
                DialogManager.showError("Нет выбранного объекта");
                return;
            }

            sceneController.copySelectedObject();
            DialogManager.showInfo("Копирование", "Объект скопирован в буфер");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.UI_OPERATION_ERROR);
        }
    }

    public void pasteObject() {
        try {
            sceneController.pasteCopiedObject();
            DialogManager.showInfo("Вставка", "Объект вставлен в сцену");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.UI_OPERATION_ERROR);
        }
    }

    public void duplicateObject() {
        try {
            if (!sceneController.hasSelectedObject()) {
                DialogManager.showError("Нет выбранного объекта");
                return;
            }

            sceneController.duplicateSelectedObject();
            DialogManager.showInfo("Дублирование", "Создана копия объекта");
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.UI_OPERATION_ERROR);
        }
    }

    public void deleteObject() {
        try {
            if (!sceneController.hasSelectedObject()) {
                DialogManager.showError("Нет выбранного объекта");
                return;
            }

            String objectName = sceneController.getSelectedObject().getName();
            Optional<ButtonType> result = DialogManager.showConfirmation(
                "Удаление объекта",
                "Удалить объект '" + objectName + "'?"
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                sceneController.removeSelectedObject();
                DialogManager.showInfo("Удаление", "Объект '" + objectName + "' удален");
            }
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.UI_OPERATION_ERROR);
        }
    }

    public void takeScreenshot() {
        try {
            ControllerUtils.getStage(anchorPane).flatMap(ScreenshotManager::takeScreenshot).ifPresent(file ->
                DialogManager.showInfo("Скриншот сохранен", "Скриншот сохранен: " + file.getName()));
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.SCREENSHOT_ERROR);
        }
    }

    public void toggleFullscreen() {
        try {
            ControllerUtils.getStage(anchorPane).ifPresent(WindowManager::toggleFullscreen);
        } catch (Exception e) {
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e,
                ru.vsu.cs.cg.utils.constants.MessageConstants.WINDOW_MANAGEMENT_ERROR);
        }
    }

    public SceneController getSceneController() {
        return sceneController;
    }
}
