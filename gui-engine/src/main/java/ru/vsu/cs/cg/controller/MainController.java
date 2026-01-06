package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.factory.ControllerFactory;
import ru.vsu.cs.cg.controller.hotkeys.HotkeyManager;
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
    @FXML private MenuItem menuEditCopy;
    @FXML private MenuItem menuEditPaste;
    @FXML private MenuItem menuEditDuplicate;
    @FXML private MenuItem menuEditDelete;
    @FXML private MenuItem menuEditSettings;
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
            hotkeyManager.registerGlobalHotkeys();

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

            ControllerFactory.injectSceneController(transformController, this);
            ControllerFactory.injectSceneController(materialController, this);

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

        sceneTreeView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(sceneTreeView.getRoot())) {
                    sceneController.handleSceneObjectSelection(newValue.getValue());
                }
            }
        );

        LOG.debug("Дерево сцены инициализировано");
    }

    private void initializeMenuActions() {
        menuThemeDark.setOnAction(event -> ControllerUtils.applyTheme(anchorPane, "/static/css/theme-dark.css"));
        menuThemeLight.setOnAction(event -> ControllerUtils.applyTheme(anchorPane, "/static/css/theme-light.css"));

        menuFileOpen.setOnAction(event -> handleFileAction(this::openModel));
        menuFileSave.setOnAction(event -> handleFileAction(this::saveSelectedModel));
        menuFileSaveAs.setOnAction(event -> handleFileAction(this::saveSelectedModel));
        menuFileExit.setOnAction(event -> handleExit());

        menuCreatePlane.setOnAction(event -> handleModelAction(() ->
            addDefaultModel(DefaultModelLoader.ModelType.PLANE)));
        menuCreateCube.setOnAction(event -> handleModelAction(() ->
            addDefaultModel(DefaultModelLoader.ModelType.CUBE)));
        menuCreateCone.setOnAction(event -> handleModelAction(() ->
            addDefaultModel(DefaultModelLoader.ModelType.CONE)));
        menuCreateCylinder.setOnAction(event -> handleModelAction(() ->
            addDefaultModel(DefaultModelLoader.ModelType.CYLINDER)));
        menuCreateTeapot.setOnAction(event -> handleModelAction(() ->
            addDefaultModel(DefaultModelLoader.ModelType.TEAPOT)));
        menuFileNewCustom.setOnAction(event -> handleModelAction(this::createCustomObject));

        menuSceneNew.setOnAction(event -> handleSceneAction(this::createNewScene));
        menuSceneOpen.setOnAction(event -> handleFileAction(this::openScene));
        menuSceneSave.setOnAction(event -> handleFileAction(this::saveScene));
        menuSceneSaveAs.setOnAction(event -> handleFileAction(this::saveSceneAs));
        menuSceneReset.setOnAction(event -> handleSceneAction(this::resetScene));

        menuWindowNew.setOnAction(event -> WindowManager.createAndShowNewWindow());
        menuWindowFullscreen.setOnAction(event ->
            ControllerUtils.getStage(anchorPane).ifPresent(WindowManager::toggleFullscreen));
        menuWindowScreenshot.setOnAction(event ->
            ControllerUtils.getStage(anchorPane).flatMap(ScreenshotManager::takeScreenshot).ifPresent(file ->
                DialogManager.showInfo("Скриншот сохранен", "Скриншот сохранен: " + file.getName())));

        menuWindowDefault.setOnAction(event -> WindowManager.arrangeDefault());
        menuWindowHorizontal.setOnAction(event -> WindowManager.arrangeHorizontally());
        menuWindowVertical.setOnAction(event -> WindowManager.arrangeVertically());
        menuWindowCascade.setOnAction(event -> WindowManager.arrangeCascade());

        menuHelpDocumentation.setOnAction(event -> ControllerUtils.openUrl("https://github.com/sphinx46/3d-viewer"));
        menuHelpShortcuts.setOnAction(event -> showHotkeysDialog());
        menuHelpBugReport.setOnAction(event -> ControllerUtils.openUrl("https://github.com/sphinx46/3d-viewer/issues/new"));
        menuHelpAbout.setOnAction(event -> showAboutDialog());

        menuEditCopy.setOnAction(event -> handleObjectAction(this::copyObject));
        menuEditPaste.setOnAction(event -> handleObjectAction(this::pasteObject));
        menuEditDuplicate.setOnAction(event -> handleObjectAction(this::duplicateObject));
        menuEditDelete.setOnAction(event -> handleObjectAction(this::deleteObject));
        menuEditSettings.setOnAction(event -> DialogManager.showInfo("Настройки", "Окно настроек в разработке"));

        menuRecentClear.setOnAction(event -> clearRecentFiles());
    }

    private void initializeButtonActions() {
        addObjectButton.setOnAction(event -> handleFileAction(this::openModel));
        deleteObjectButton.setOnAction(event -> handleObjectAction(this::deleteObject));
        duplicateObjectButton.setOnAction(event -> handleObjectAction(this::duplicateObject));
    }

    public void updateSceneTree() {
        Platform.runLater(() -> {
            try {
                TreeItem<String> rootItem = sceneTreeView.getRoot();
                if (rootItem == null) {
                    rootItem = new TreeItem<>("Сцена");
                    rootItem.setExpanded(true);
                    sceneTreeView.setRoot(rootItem);
                }

                rootItem.getChildren().clear();

                TreeItem<String> finalRootItem = rootItem;
                sceneController.getCurrentScene().getObjects().forEach(obj -> {
                    String displayName = obj.isVisible() ? obj.getName() : obj.getName() + " (скрыт)";
                    TreeItem<String> item = new TreeItem<>(displayName);
                    finalRootItem.getChildren().add(item);
                });

                sceneTreeView.refresh();
                LOG.debug("Дерево сцены обновлено. Объектов: {}", rootItem.getChildren().size());
            } catch (Exception e) {
                LOG.error("Ошибка обновления дерева сцены: {}", e.getMessage());
            }
        });
    }

    private void loadRecentFiles() {
        try {
            List<String> recentFiles = CachePersistenceManager.loadRecentFiles();
            if (recentFiles != null) {
                recentFiles.forEach(recentFilesCacheService::addFile);
                LOG.info("Загружено {} недавних файлов из кеша", recentFiles.size());
            } else {
                LOG.info("Список недавних файлов пуст");
            }
            updateRecentFilesMenu();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки списка недавних файлов: {}", e.getMessage());
        }
    }

    private void updateRecentFilesMenu() {
        try {
            menuRecent.getItems().removeIf(item -> !item.equals(menuRecentClear) && !(item instanceof SeparatorMenuItem));

            List<String> recentFiles = recentFilesCacheService.getRecentFiles();
            if (recentFiles.isEmpty()) {
                MenuItem emptyItem = new MenuItem("Нет недавних файлов");
                emptyItem.setDisable(true);
                menuRecent.getItems().add(emptyItem);
            } else {
                recentFiles.forEach(filePath -> {
                    String fileName = ControllerUtils.getFileName(filePath);
                    MenuItem fileItem = new MenuItem(fileName);
                    fileItem.setOnAction(event -> openRecentFile(filePath));
                    menuRecent.getItems().add(fileItem);
                });
            }

            LOG.debug("Меню недавних файлов обновлено: {} файлов", recentFiles.size());
        } catch (Exception e) {
            LOG.error("Ошибка обновления меню недавних файлов: {}", e.getMessage());
        }
    }

    private void openRecentFile(String filePath) {
        try {
            boolean isSceneFormat = ControllerUtils.isSceneFormat(filePath);
            if (isSceneFormat) {
                sceneController.loadScene(filePath);
                DialogManager.showSuccess("Сцена загружена: " + ControllerUtils.getFileName(filePath));
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

    private void showHotkeysDialog() {
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

            hotkeysText.append("\nРедактирование:\n");
            hotkeysText.append("  G               - ").append(descriptions.get("G")).append("\n");
            hotkeysText.append("  R               - ").append(descriptions.get("R")).append("\n");
            hotkeysText.append("  S               - ").append(descriptions.get("S")).append("\n");

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
            Optional<ButtonType> result = DialogManager.showConfirmation(
                "Подтверждение выхода",
                "Вы уверены, что хотите выйти из приложения?"
            );

            if (result.isPresent() && result.get() == ButtonType.OK) {
                CachePersistenceManager.saveRecentFiles(recentFilesCacheService.getRecentFiles());
                hotkeyManager.unregisterGlobalHotkeys();
                Platform.exit();
                LOG.info("Пользователь завершил работу приложения");
            }
        } catch (Exception e) {
            LOG.error("Ошибка завершения работы приложения: {}", e.getMessage());
            Platform.exit();
        }
    }

    private void handleFileAction(Runnable fileOperation) {
        ControllerUtils.getStage(anchorPane).ifPresent(stage -> fileOperation.run());
    }

    private void handleModelAction(Runnable modelOperation) {
        try {
            modelOperation.run();
        } catch (Exception e) {
            LOG.error("Ошибка операции с моделью: {}", e.getMessage());
            DialogManager.showError("Ошибка операции с моделью: " + e.getMessage());
        }
    }

    private void handleSceneAction(Runnable sceneOperation) {
        try {
            sceneOperation.run();
        } catch (Exception e) {
            LOG.error("Ошибка операции со сценой: {}", e.getMessage());
            DialogManager.showError("Ошибка операции со сценой: " + e.getMessage());
        }
    }

    private void handleObjectAction(Runnable objectOperation) {
        try {
            objectOperation.run();
        } catch (Exception e) {
            LOG.error("Ошибка операции с объектом: {}", e.getMessage());
            DialogManager.showError("Ошибка операции с объектом: " + e.getMessage());
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

    private void createCustomObject() {
        sceneController.createCustomObject();
        DialogManager.showInfo("Пользовательский объект", "Создан новый куб для редактирования");
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

    private void createNewScene() {
        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Новая сцена",
            "Создать новую сцену? Несохраненные изменения будут потеряны."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            sceneController.createNewScene();
            DialogManager.showInfo("Новая сцена", "Создана новая сцена");
        }
    }

    private void openScene() {
        ControllerUtils.getStage(anchorPane).flatMap(DialogManager::showOpenSceneDialog).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();
                sceneController.loadScene(filePath);
                recentFilesCacheService.addFile(filePath);
                updateRecentFilesMenu();
                DialogManager.showSuccess("Сцена загружена: " + file.getName());
            } catch (Exception e) {
                LOG.error("Ошибка загрузки сцены: {}", e.getMessage());
                DialogManager.showError("Ошибка загрузки сцены: " + e.getMessage());
            }
        });
    }

    private void saveScene() {
        ControllerUtils.getStage(anchorPane).flatMap(stage ->
            DialogManager.showSaveSceneDialog(stage, sceneController.getCurrentScene().getName())).ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();
                sceneController.saveScene(filePath);
                recentFilesCacheService.addFile(filePath);
                updateRecentFilesMenu();
                DialogManager.showSuccess("Сцена сохранена: " + file.getName());
            } catch (Exception e) {
                LOG.error("Ошибка сохранения сцены: {}", e.getMessage());
                DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
            }
        });
    }

    private void saveSceneAs() {
        saveScene();
    }

    private void resetScene() {
        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Сброс сцены",
            "Сбросить все изменения в сцене?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            sceneController.createNewScene();
            DialogManager.showInfo("Сброс сцены", "Сцена сброшена до начального состояния");
        }
    }

    private void copyObject() {
        if (!sceneController.hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        sceneController.copySelectedObject();
        DialogManager.showInfo("Копирование", "Объект скопирован в буфер");
    }

    private void pasteObject() {
        sceneController.pasteCopiedObject();
        DialogManager.showInfo("Вставка", "Объект вставлен в сцену");
    }

    private void duplicateObject() {
        if (!sceneController.hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        sceneController.duplicateSelectedObject();
        DialogManager.showInfo("Дублирование", "Создана копия объекта");
    }

    private void deleteObject() {
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

    public SceneController getSceneController() {
        return sceneController;
    }
}
