package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.factory.ControllerFactory;
import ru.vsu.cs.cg.controller.hotkeys.HotkeyManager;
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
            LOG.error("Ошибка инициализации главного контроллера: {}", e.getMessage());
            DialogManager.showError("Ошибка инициализации приложения: " + e.getMessage());
        }
    }

    private void initializeControllers() {
        transformController = ControllerFactory.createController("/fxml/transform-panel.fxml", TransformController.class);
        materialController = ControllerFactory.createController("/fxml/material-panel.fxml", MaterialController.class);
    }

    private void initializeDependencies() {
        try {
            this.sceneController.setMainController(this);
            this.sceneController.setTransformController(transformController);
            this.sceneController.setMaterialController(materialController);

            LOG.debug("Зависимости контроллеров успешно инициализированы");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации зависимостей контроллеров: {}", e.getMessage());
        }
    }

    private void initializeTooltips() {
        TooltipManager.addHotkeyTooltip(addObjectButton, "addObjectButton");
        TooltipManager.addHotkeyTooltip(deleteObjectButton, "deleteObjectButton");
        TooltipManager.addHotkeyTooltip(duplicateObjectButton, "duplicateObjectButton");
        TooltipManager.addHotkeyTooltip(selectToolButton, "selectToolButton");
        TooltipManager.addHotkeyTooltip(moveToolButton, "moveToolButton");
        TooltipManager.addHotkeyTooltip(rotateToolButton, "rotateToolButton");
        TooltipManager.addHotkeyTooltip(scaleToolButton, "scaleToolButton");
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
        menuThemeDark.setOnAction(event -> ControllerUtils.applyTheme(anchorPane, "/static/css/theme-dark.css"));
        menuThemeLight.setOnAction(event -> ControllerUtils.applyTheme(anchorPane, "/static/css/theme-light.css"));

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

        menuWindowNew.setOnAction(event -> WindowManager.createAndShowNewWindow());
        menuWindowFullscreen.setOnAction(event -> toggleFullscreen());
        menuWindowScreenshot.setOnAction(event -> takeScreenshot());

        menuWindowDefault.setOnAction(event -> WindowManager.arrangeDefault());
        menuWindowHorizontal.setOnAction(event -> WindowManager.arrangeHorizontally());
        menuWindowVertical.setOnAction(event -> WindowManager.arrangeVertically());
        menuWindowCascade.setOnAction(event -> WindowManager.arrangeCascade());

        menuHelpDocumentation.setOnAction(event -> ControllerUtils.openUrl("https://github.com/sphinx46/3d-viewer"));
        menuHelpShortcuts.setOnAction(event -> showHotkeysDialog());
        menuHelpBugReport.setOnAction(event -> ControllerUtils.openUrl("https://github.com/sphinx46/3d-viewer/issues/new"));
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
                LOG.error("Ошибка обновления дерева сцены: {}", e.getMessage());
            }
        });
    }

    private void loadRecentFiles() {
        try {
            List<String> recentFiles = CachePersistenceManager.loadRecentFiles();
            recentFiles.forEach(recentFilesCacheService::addFile);
            LOG.info("Загружено {} недавних файлов из кеша", recentFiles.size());
            updateRecentFilesMenu();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки списка недавних файлов: {}", e.getMessage());
        }
    }

    private void updateRecentFilesMenu() {
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
            LOG.error("Ошибка обновления меню недавних файлов: {}", e.getMessage());
        }
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
            LOG.error("Ошибка загрузки недавнего файла '{}': {}", filePath, e.getMessage());
            DialogManager.showError("Не удалось загрузить файл: " + ControllerUtils.getFileName(filePath));
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
            LOG.error("Ошибка показа диалога горячих клавиш: {}", e.getMessage());
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
            LOG.error("Ошибка показа окна 'О программе': {}", e.getMessage());
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
            LOG.error("Ошибка очистки списка недавних файлов: {}", e.getMessage());
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
            LOG.error("Ошибка завершения работы приложения: {}", e.getMessage());
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
                LOG.error("Ошибка добавления модели: {}", e.getMessage());
                DialogManager.showError("Ошибка добавления модели: " + e.getMessage());
            }
        });
    }

    public void createCustomObject() {
        sceneController.createCustomObject();
        DialogManager.showInfo("Пользовательский объект", "Создан новый объект для редактирования");
    }

    private void addDefaultModel(DefaultModelLoader.ModelType modelType) {
        sceneController.addDefaultModelToScene(modelType);
        DialogManager.showSuccess("Модель '" + modelType.getDisplayName() + "' добавлена");
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
                LOG.error("Ошибка сохранения модели: {}", e.getMessage());
                DialogManager.showError("Ошибка сохранения модели: " + e.getMessage());
            }
        });
    }

    public void createNewScene() {
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
    }

    public void openSceneWithCheck() {
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
    }

    private void openSceneWithCheck(String filePath) {
        if (sceneController.hasUnsavedChanges()) {
            Optional<ButtonType> result = DialogManager.showConfirmation(
                "Несохраненные изменения",
                "В текущей сцене есть несохраненные изменения. Загрузить сцену из файла?"
            );

            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        try {
            sceneController.loadScene(filePath);
            DialogManager.showSuccess("Сцена загружена: " + ControllerUtils.getFileName(filePath));
            recentFilesCacheService.addFile(filePath);
            updateRecentFilesMenu();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки сцены: {}", e.getMessage());
            DialogManager.showError("Ошибка загрузки сцены: " + e.getMessage());
        }
    }

    private void openScene() {
        ControllerUtils.getStage(anchorPane).flatMap(DialogManager::showOpenSceneDialog).ifPresent(file -> {
            openSceneWithCheck(file.getAbsolutePath());
        });
    }

    public void saveScene() {
        if (sceneController.getCurrentSceneFilePath() != null) {
            try {
                sceneController.saveScene(sceneController.getCurrentSceneFilePath());
                DialogManager.showSuccess("Сцена сохранена");
            } catch (Exception e) {
                LOG.error("Ошибка сохранения сцены: {}", e.getMessage());
                DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
            }
        } else {
            saveSceneAs();
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
                DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
            }
        });
    }

    private void resetScene() {
        if (sceneController.hasUnsavedChanges()) {
            Optional<ButtonType> result = DialogManager.showConfirmation(
                "Сброс сцены",
                "Сбросить все изменения в сцене? Несохраненные изменения будут потеряны."
            );

            if (result.isEmpty() || result.get() != ButtonType.OK) return;
        }

        sceneController.createNewScene();
        DialogManager.showInfo("Сброс сцены", "Сцена сброшена до начального состояния");
    }

    public void copyObject() {
        if (!sceneController.hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        sceneController.copySelectedObject();
        DialogManager.showInfo("Копирование", "Объект скопирован в буфер");
    }

    public void pasteObject() {
        sceneController.pasteCopiedObject();
        DialogManager.showInfo("Вставка", "Объект вставлен в сцену");
    }

    public void duplicateObject() {
        if (!sceneController.hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        sceneController.duplicateSelectedObject();
        DialogManager.showInfo("Дублирование", "Создана копия объекта");
    }

    public void deleteObject() {
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
    }

    public void takeScreenshot() {
        ControllerUtils.getStage(anchorPane).flatMap(ScreenshotManager::takeScreenshot).ifPresent(file ->
            DialogManager.showInfo("Скриншот сохранен", "Скриншот сохранен: " + file.getName()));
    }

    public void toggleFullscreen() {
        ControllerUtils.getStage(anchorPane).ifPresent(WindowManager::toggleFullscreen);
    }

    public SceneController getSceneController() {
        return sceneController;
    }
}
