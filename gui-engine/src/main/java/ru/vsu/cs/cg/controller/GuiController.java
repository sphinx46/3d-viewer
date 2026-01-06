package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.service.RecentFilesCacheService;
import ru.vsu.cs.cg.service.SceneService;
import ru.vsu.cs.cg.service.impl.ModelServiceImpl;
import ru.vsu.cs.cg.service.impl.RecentFilesCacheServiceImpl;
import ru.vsu.cs.cg.service.impl.SceneServiceImpl;
import ru.vsu.cs.cg.utils.*;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GuiController {

    private static final Logger LOG = LoggerFactory.getLogger(GuiController.class);
    private static final String DARK_THEME = "/static/css/theme-dark.css";
    private static final String LIGHT_THEME = "/static/css/theme-light.css";
    private static final String GITHUB_ISSUES_URL = "https://github.com/sphinx46/3d-viewer/issues/new";
    private static final String GITHUB_DOCS_URL = "https://github.com/sphinx46/3d-viewer";

    private final ModelService modelService = new ModelServiceImpl();
    private final RecentFilesCacheService recentFilesCacheService = new RecentFilesCacheServiceImpl();
    private final SceneService sceneService = new SceneServiceImpl(modelService);
    private final Map<Slider, TextField> sliderBindings = new HashMap<>();

    private Scene currentScene;
    private final StringProperty selectedObjectName = new SimpleStringProperty("Нет выбора");

    @FXML private AnchorPane anchorPane;
    @FXML private StackPane viewerContainer;

    @FXML private TreeView<String> sceneTreeView;

    @FXML private CheckBox visibilityCheckbox;
    @FXML private TextField objectNameField;
    @FXML private TextField positionX;
    @FXML private TextField positionY;
    @FXML private TextField positionZ;
    @FXML private TextField rotationX;
    @FXML private TextField rotationY;
    @FXML private TextField rotationZ;
    @FXML private TextField scaleX;
    @FXML private TextField scaleY;
    @FXML private TextField scaleZ;

    @FXML private ColorPicker colorPicker;
    @FXML private TextField materialShininessField;
    @FXML private TextField materialTransparencyField;
    @FXML private TextField materialReflectivityField;

    @FXML private Button applyTransformButton;
    @FXML private Button resetTransformButton;
    @FXML private Button addObjectButton;
    @FXML private Button deleteObjectButton;
    @FXML private Button duplicateObjectButton;

    @FXML private TextField vertexIndexField;
    @FXML private Button selectVertexButton;
    @FXML private Button deleteVertexButton;
    @FXML private TextField polygonIndexField;
    @FXML private Button selectPolygonButton;
    @FXML private Button deletePolygonButton;
    @FXML private Button smoothButton;
    @FXML private Button subdivideButton;

    @FXML private Button loadTextureButton;
    @FXML private Button clearTextureButton;
    @FXML private CheckBox showTextureCheckbox;

    @FXML private Slider brightnessSlider;
    @FXML private TextField brightnessField;
    @FXML private Slider reflectionSlider;
    @FXML private TextField reflectionField;

    @FXML private TextField cameraPositionX;
    @FXML private TextField cameraPositionY;
    @FXML private TextField cameraPositionZ;
    @FXML private TextField cameraDirectionX;
    @FXML private TextField cameraDirectionY;
    @FXML private TextField cameraDirectionZ;
    @FXML private Slider focalLengthSlider;
    @FXML private TextField focalLengthField;
    @FXML private Slider sensitivitySlider;
    @FXML private TextField sensitivityField;
    @FXML private Button resetCameraButton;
    @FXML private Button applyCameraButton;

    @FXML private MenuItem menuThemeDark;
    @FXML private MenuItem menuThemeLight;

    @FXML private MenuItem menuFileOpen;
    @FXML private MenuItem menuFileSave;
    @FXML private MenuItem menuFileNewCustom;
    @FXML private MenuItem menuFileSaveAs;
    @FXML private MenuItem menuFileExit;
    @FXML private MenuItem menuFileReset;
    @FXML private MenuItem menuFileNewScene;
    @FXML private MenuItem menuFileOpenScene;
    @FXML private MenuItem menuFileSaveScene;
    @FXML private MenuItem menuFileSaveSceneAs;

    @FXML private MenuItem menuEditUndo;
    @FXML private MenuItem menuEditHistory;
    @FXML private MenuItem menuEditRedo;
    @FXML private MenuItem menuEditSettings;

    @FXML private MenuItem menuRecentClear;
    @FXML private Menu menuRecent;

    @FXML private MenuItem menuCreatePlane;
    @FXML private MenuItem menuCreateCube;
    @FXML private MenuItem menuCreateCone;
    @FXML private MenuItem menuCreateCylinder;
    @FXML private MenuItem menuCreateTeapot;

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

    @FXML private Button selectToolButton;
    @FXML private Button moveToolButton;
    @FXML private Button rotateToolButton;
    @FXML private Button scaleToolButton;

    @FXML
    private void initialize() {
        LOG.info("Инициализация GuiController");

        try {
            currentScene = sceneService.createNewScene();
            initializeTooltips();
            loadRecentFilesFromCache();
            initializeSliderBindings();
            initializeSceneTree();
            initializeTransformBindings();
            initializeMaterialBindings();
            initializeButtonActions();
            initializeMenuActions();
            updateRecentFilesMenu();
            registerCurrentStage();
            updateUIFromScene();
            LOG.debug("GuiController успешно инициализирован");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации GuiController", e);
            throw new RuntimeException("Ошибка инициализации контроллера", e);
        }
    }

    private void initializeTooltips() {
        Tooltip selectTooltip = new Tooltip("Выделить объект");
        Tooltip moveTooltip = new Tooltip("Переместить объект");
        Tooltip rotateTooltip = new Tooltip("Повернуть объект");
        Tooltip scaleTooltip = new Tooltip("Масштабировать объект");
        Tooltip addTooltip = new Tooltip("Добавить объект");
        Tooltip deleteTooltip = new Tooltip("Удалить выбранный объект");
        Tooltip duplicateTooltip = new Tooltip("Дублировать выбранный объект");
        Tooltip applyTooltip = new Tooltip("Применить трансформацию");
        Tooltip resetTooltip = new Tooltip("Сбросить трансформацию");

        selectTooltip.setShowDelay(javafx.util.Duration.millis(500));
        moveTooltip.setShowDelay(javafx.util.Duration.millis(500));
        rotateTooltip.setShowDelay(javafx.util.Duration.millis(500));
        scaleTooltip.setShowDelay(javafx.util.Duration.millis(500));
        addTooltip.setShowDelay(javafx.util.Duration.millis(500));
        deleteTooltip.setShowDelay(javafx.util.Duration.millis(500));
        duplicateTooltip.setShowDelay(javafx.util.Duration.millis(500));
        applyTooltip.setShowDelay(javafx.util.Duration.millis(500));
        resetTooltip.setShowDelay(javafx.util.Duration.millis(500));

        Tooltip.install(selectToolButton, selectTooltip);
        Tooltip.install(moveToolButton, moveTooltip);
        Tooltip.install(rotateToolButton, rotateTooltip);
        Tooltip.install(scaleToolButton, scaleTooltip);
        Tooltip.install(addObjectButton, addTooltip);
        Tooltip.install(deleteObjectButton, deleteTooltip);
        Tooltip.install(duplicateObjectButton, duplicateTooltip);
        Tooltip.install(applyTransformButton, applyTooltip);
        Tooltip.install(resetTransformButton, resetTooltip);

        LOG.debug("Инициализированы подсказки для инструментов");
    }

    private void initializeSceneTree() {
        TreeItem<String> rootItem = new TreeItem<>("Сцена");
        rootItem.setExpanded(true);
        sceneTreeView.setRoot(rootItem);
        sceneTreeView.setShowRoot(true);

        sceneTreeView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(rootItem)) {
                    String objectName = newValue.getValue();
                    selectSceneObjectByName(objectName);
                }
            }
        );

        LOG.debug("Дерево сцены инициализировано");
    }

    private void initializeTransformBindings() {
        StringConverter<Number> numberConverter = new NumberStringConverter() {
            @Override
            public Number fromString(String string) {
                try {
                    return Double.parseDouble(string.replace(',', '.'));
                } catch (NumberFormatException e) {
                    LOG.warn("Неверный формат числа: '{}'", string);
                    return 0.0;
                }
            }
        };

        LOG.debug("Привязки трансформации инициализированы");
    }

    private void initializeMaterialBindings() {
        LOG.debug("Привязки материала инициализированы");
    }

    private void initializeButtonActions() {
        addObjectButton.setOnAction(event -> addNewObject());
        deleteObjectButton.setOnAction(event -> deleteSelectedObject());
        duplicateObjectButton.setOnAction(event -> duplicateSelectedObject());
        applyTransformButton.setOnAction(event -> applyTransformToSelectedObject());
        resetTransformButton.setOnAction(event -> resetTransformOfSelectedObject());

        objectNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (currentScene.getSelectedObject() != null) {
                currentScene.getSelectedObject().setName(newValue);
                updateSceneTree();
            }
        });

        visibilityCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (currentScene.getSelectedObject() != null) {
                currentScene.getSelectedObject().setVisible(newValue);
                updateSceneTree();
            }
        });

        LOG.debug("Действия кнопок инициализированы");
    }

    private void loadRecentFilesFromCache() {
        List<String> recentFiles = CachePersistenceManager.loadRecentFiles();
        for (String filePath : recentFiles) {
            recentFilesCacheService.addFile(filePath);
        }
        LOG.info("Загружено {} файлов из кеша", recentFiles.size());
    }

    private void initializeSliderBindings() {
        sliderBindings.put(brightnessSlider, brightnessField);
        sliderBindings.put(reflectionSlider, reflectionField);
        sliderBindings.put(focalLengthSlider, focalLengthField);
        sliderBindings.put(sensitivitySlider, sensitivityField);

        sliderBindings.forEach((slider, textField) ->
            textField.textProperty().bindBidirectional(
                slider.valueProperty(),
                createNumberConverter(slider)
            )
        );

        LOG.debug("Собрано и настроено {} привязок слайдеров", sliderBindings.size());
    }

    private StringConverter<Number> createNumberConverter(Slider slider) {
        return new NumberStringConverter() {
            @Override
            public String toString(Number value) {
                return slider == focalLengthSlider ?
                    String.format("%.0f", value.doubleValue()) :
                    String.format("%.2f", value.doubleValue());
            }

            @Override
            public Number fromString(String string) {
                try {
                    double value = Double.parseDouble(string.replace(',', '.'));
                    return Math.max(slider.getMin(), Math.min(value, slider.getMax()));
                } catch (NumberFormatException e) {
                    LOG.warn("Неверный формат числа: '{}'", string);
                    return slider.getValue();
                }
            }
        };
    }

    private void initializeMenuActions() {
        menuThemeDark.setOnAction(event -> applyTheme(DARK_THEME));
        menuThemeLight.setOnAction(event -> applyTheme(LIGHT_THEME));

        menuFileOpen.setOnAction(event -> openModelAndAddToScene());
        menuFileNewCustom.setOnAction(event -> createCustomObject());
        menuFileSaveAs.setOnAction(event -> saveSelectedModelToFile());
        menuFileExit.setOnAction(event -> handleExit());

        menuFileNewScene.setOnAction(event -> createNewScene());
        menuFileOpenScene.setOnAction(event -> openSceneFromFile());
        menuFileSaveScene.setOnAction(event -> saveSceneToFile());
        menuFileSaveSceneAs.setOnAction(event -> saveSceneAsToFile());

        menuRecentClear.setOnAction(event -> clearRecentFiles());

        menuCreatePlane.setOnAction(event -> addDefaultModelToScene(DefaultModelLoader.ModelType.PLANE));
        menuCreateCube.setOnAction(event -> addDefaultModelToScene(DefaultModelLoader.ModelType.CUBE));
        menuCreateCone.setOnAction(event -> addDefaultModelToScene(DefaultModelLoader.ModelType.CONE));
        menuCreateCylinder.setOnAction(event -> addDefaultModelToScene(DefaultModelLoader.ModelType.CYLINDER));
        menuCreateTeapot.setOnAction(event -> addDefaultModelToScene(DefaultModelLoader.ModelType.TEAPOT));

        menuWindowNew.setOnAction(event -> {
            LOG.info("Создание нового окна");
            WindowManager.createAndShowNewWindow();
        });
        menuWindowFullscreen.setOnAction(event -> getStage().ifPresent(WindowManager::toggleFullscreen));
        menuWindowScreenshot.setOnAction(event -> getStage().ifPresent(stage -> {
            LOG.info("Создание скриншота");
            Optional<File> screenshotFile = ScreenshotManager.takeScreenshot(stage);
            screenshotFile.ifPresent(file ->
                DialogManager.showInfo("Скриншот сохранен", "Скриншот сохранен: " + file.getName())
            );
        }));
        menuWindowDefault.setOnAction(event -> WindowManager.arrangeDefault());
        menuWindowHorizontal.setOnAction(event -> WindowManager.arrangeHorizontally());
        menuWindowVertical.setOnAction(event -> WindowManager.arrangeVertically());
        menuWindowCascade.setOnAction(event -> WindowManager.arrangeCascade());

        menuHelpDocumentation.setOnAction(event -> openUrl(GITHUB_DOCS_URL));
        menuHelpBugReport.setOnAction(event -> openUrl(GITHUB_ISSUES_URL));
        menuHelpAbout.setOnAction(event -> {
            String aboutText = "3d-viewer\n" +
                "Версия: 0.0.1\n" +
                "Разработчики: sphinx46, Senpaka, Y66dras1ll\n" +
                "Программа для просмотра и редактирования 3D моделей.\n" +
                "Поддержка множественных объектов и сцен.";
            DialogManager.showInfo("О программе", aboutText);
        });
    }

    private void createNewScene() {
        LOG.info("Создание новой сцены");

        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Новая сцена",
            "Создать новую сцену? Несохраненные изменения будут потеряны."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentScene = sceneService.createNewScene();
            updateUIFromScene();
            DialogManager.showInfo("Новая сцена", "Создана новая сцена");
        }
    }

    private void openSceneFromFile() {
        getStage().ifPresentOrElse(
            stage -> {
                Optional<File> fileOptional = DialogManager.showOpenSceneDialog(stage);
                fileOptional.ifPresent(file -> {
                    try {
                        String filePath = file.getAbsolutePath();
                        currentScene = sceneService.loadScene(filePath);
                        recentFilesCacheService.addFile(filePath);
                        updateRecentFilesMenu();
                        updateUIFromScene();
                        DialogManager.showSuccess("Сцена успешно загружена: " + file.getName());
                    } catch (Exception e) {
                        LOG.error("Ошибка загрузки сцены: {}", e.getMessage(), e);
                        DialogManager.showError("Ошибка загрузки сцены: " + e.getMessage());
                    }
                });
            },
            () -> DialogManager.showError("Не удалось определить окно приложения")
        );
    }

    private void saveSceneToFile() {
        if (currentScene == null) {
            DialogManager.showError("Нет сцены для сохранения");
            return;
        }

        getStage().ifPresentOrElse(
            stage -> {
                Optional<File> fileOptional = DialogManager.showSaveSceneDialog(stage, currentScene.getName());
                fileOptional.ifPresent(file -> {
                    try {
                        String filePath = file.getAbsolutePath();
                        sceneService.saveScene(currentScene, filePath);
                        recentFilesCacheService.addFile(filePath);
                        updateRecentFilesMenu();
                        DialogManager.showSuccess("Сцена сохранена: " + file.getName());
                    } catch (Exception e) {
                        LOG.error("Ошибка сохранения сцены: {}", e.getMessage(), e);
                        DialogManager.showError("Ошибка сохранения сцены: " + e.getMessage());
                    }
                });
            },
            () -> DialogManager.showError("Не удалось определить окно приложения")
        );
    }

    private void saveSceneAsToFile() {
        saveSceneToFile();
    }

    private void addNewObject() {
        LOG.info("Добавление нового объекта в сцену");

        getStage().ifPresentOrElse(
            stage -> {
                Optional<File> fileOptional = DialogManager.showOpenModelDialog(stage);
                fileOptional.ifPresent(file -> {
                    try {
                        String filePath = file.getAbsolutePath();
                        SceneObject newObject = sceneService.addModelToScene(currentScene, filePath);
                        currentScene.selectObject(newObject);
                        updateUIFromScene();
                        recentFilesCacheService.addFile(filePath);
                        updateRecentFilesMenu();
                        DialogManager.showSuccess("Объект добавлен: " + newObject.getName());
                    } catch (Exception e) {
                        LOG.error("Ошибка добавления объекта: {}", e.getMessage(), e);
                        DialogManager.showError("Ошибка добавления объекта: " + e.getMessage());
                    }
                });
            },
            () -> DialogManager.showError("Не удалось определить окно приложения")
        );
    }

    private void deleteSelectedObject() {
        if (currentScene.getSelectedObject() == null) {
            DialogManager.showError("Нет выбранного объекта для удаления");
            return;
        }

        String objectName = currentScene.getSelectedObject().getName();
        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Удаление объекта",
            "Удалить объект '" + objectName + "'?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            sceneService.removeSelectedObject(currentScene);
            updateUIFromScene();
            DialogManager.showInfo("Объект удален", "Объект '" + objectName + "' удален");
        }
    }

    private void duplicateSelectedObject() {
        if (currentScene.getSelectedObject() == null) {
            DialogManager.showError("Нет выбранного объекта для дублирования");
            return;
        }

        sceneService.duplicateSelectedObject(currentScene);
        updateUIFromScene();
        DialogManager.showInfo("Объект продублирован", "Создана копия объекта");
    }

    private void applyTransformToSelectedObject() {
        if (currentScene.getSelectedObject() == null) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        try {
            SceneObject selected = currentScene.getSelectedObject();

            selected.getTransform().setPositionX(parseDouble(positionX.getText()));
            selected.getTransform().setPositionY(parseDouble(positionY.getText()));
            selected.getTransform().setPositionZ(parseDouble(positionZ.getText()));

            selected.getTransform().setRotationX(parseDouble(rotationX.getText()));
            selected.getTransform().setRotationY(parseDouble(rotationY.getText()));
            selected.getTransform().setRotationZ(parseDouble(rotationZ.getText()));

            selected.getTransform().setScaleX(parseDouble(scaleX.getText()));
            selected.getTransform().setScaleY(parseDouble(scaleY.getText()));
            selected.getTransform().setScaleZ(parseDouble(scaleZ.getText()));

            LOG.info("Трансформация применена к объекту '{}'", selected.getName());
            DialogManager.showInfo("Трансформация применена", "Параметры трансформации обновлены");

        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга чисел трансформации: {}", e.getMessage());
            DialogManager.showError("Ошибка в данных трансформации. Проверьте формат чисел.");
        }
    }

    private void resetTransformOfSelectedObject() {
        if (currentScene.getSelectedObject() == null) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        currentScene.getSelectedObject().getTransform().reset();
        updateUIFromSelectedObject();
        LOG.info("Трансформация объекта '{}' сброшена", currentScene.getSelectedObject().getName());
        DialogManager.showInfo("Трансформация сброшена", "Трансформация установлена по умолчанию");
    }

    private double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(text.replace(',', '.'));
    }

    private void selectSceneObjectByName(String objectName) {
        currentScene.findObjectByName(objectName).ifPresentOrElse(
            object -> {
                currentScene.selectObject(object);
                updateUIFromSelectedObject();
                LOG.debug("Выбран объект: {}", objectName);
            },
            () -> LOG.warn("Объект с именем '{}' не найден", objectName)
        );
    }

    private void updateUIFromScene() {
        updateSceneTree();
        updateUIFromSelectedObject();
        updateSceneInfo();
    }

    private void updateSceneTree() {
        TreeItem<String> rootItem = sceneTreeView.getRoot();
        rootItem.getChildren().clear();

        for (SceneObject obj : currentScene.getObjects()) {
            TreeItem<String> item = new TreeItem<>(obj.getName());
            if (!obj.isVisible()) {
                item.setValue(obj.getName() + " (скрыт)");
            }
            rootItem.getChildren().add(item);
        }

        sceneTreeView.refresh();
        LOG.debug("Дерево сцены обновлено. Объектов: {}", currentScene.getObjectCount());
    }

    private void updateUIFromSelectedObject() {
        SceneObject selected = currentScene.getSelectedObject();

        if (selected == null) {
            clearObjectFields();
            selectedObjectName.set("Нет выбора");
            setObjectFieldsEditable(false);
        } else {
            populateObjectFields(selected);
            selectedObjectName.set(selected.getName());
            setObjectFieldsEditable(true);
        }
    }

    private void clearObjectFields() {
        objectNameField.clear();
        visibilityCheckbox.setSelected(false);

        positionX.clear();
        positionY.clear();
        positionZ.clear();
        rotationX.clear();
        rotationY.clear();
        rotationZ.clear();
        scaleX.clear();
        scaleY.clear();
        scaleZ.clear();

        colorPicker.setValue(Color.WHITE);
        materialShininessField.clear();
        materialReflectivityField.clear();
        materialTransparencyField.clear();
    }

    private void populateObjectFields(SceneObject object) {
        objectNameField.setText(object.getName());
        visibilityCheckbox.setSelected(object.isVisible());

        positionX.setText(String.format("%.3f", object.getTransform().getPositionX()));
        positionY.setText(String.format("%.3f", object.getTransform().getPositionY()));
        positionZ.setText(String.format("%.3f", object.getTransform().getPositionZ()));

        rotationX.setText(String.format("%.3f", object.getTransform().getRotationX()));
        rotationY.setText(String.format("%.3f", object.getTransform().getRotationY()));
        rotationZ.setText(String.format("%.3f", object.getTransform().getRotationZ()));

        scaleX.setText(String.format("%.3f", object.getTransform().getScaleX()));
        scaleY.setText(String.format("%.3f", object.getTransform().getScaleY()));
        scaleZ.setText(String.format("%.3f", object.getTransform().getScaleZ()));

        colorPicker.setValue(object.getMaterial().getColor());
        materialShininessField.setText(String.format("%.3f", object.getMaterial().getShininess()));
        materialReflectivityField.setText(String.format("%.3f", object.getMaterial().getReflectivity()));
        materialTransparencyField.setText(String.format("%.3f", object.getMaterial().getTransparency()));
    }

    private void setObjectFieldsEditable(boolean editable) {
        objectNameField.setEditable(editable);
        visibilityCheckbox.setDisable(!editable);

        positionX.setEditable(editable);
        positionY.setEditable(editable);
        positionZ.setEditable(editable);
        rotationX.setEditable(editable);
        rotationY.setEditable(editable);
        rotationZ.setEditable(editable);
        scaleX.setEditable(editable);
        scaleY.setEditable(editable);
        scaleZ.setEditable(editable);

        colorPicker.setDisable(!editable);
        materialShininessField.setEditable(editable);
        materialReflectivityField.setEditable(editable);
        materialTransparencyField.setEditable(editable);

        applyTransformButton.setDisable(!editable);
        resetTransformButton.setDisable(!editable);
        deleteObjectButton.setDisable(!editable);
        duplicateObjectButton.setDisable(!editable);
    }

    private void updateSceneInfo() {
        LOG.debug("Информация о сцене обновлена: name='{}', objects={}, selected={}",
            currentScene.getName(), currentScene.getObjectCount(),
            currentScene.getSelectedObject() != null ? currentScene.getSelectedObject().getName() : "null");
    }

    private void createCustomObject() {
        LOG.info("Создание пользовательского объекта");

        try {
            SceneObject newObject = sceneService.addDefaultModelToScene(currentScene, "CUBE");
            currentScene.selectObject(newObject);
            updateUIFromScene();
            DialogManager.showInfo(
                MessageConstants.EMPTY_OBJECT_CREATED,
                MessageConstants.EMPTY_OBJECT_DETAILS
            );
        } catch (Exception e) {
            LOG.error("Ошибка создания пользовательского объекта: {}", e.getMessage(), e);
            DialogManager.showError("Ошибка создания пользовательского объекта: " + e.getMessage());
        }
    }

    private void addDefaultModelToScene(DefaultModelLoader.ModelType modelType) {
        LOG.info("Добавление стандартной модели в сцену: {}", modelType.getDisplayName());

        try {
            SceneObject newObject = sceneService.addDefaultModelToScene(currentScene, modelType.name());
            currentScene.selectObject(newObject);
            updateUIFromScene();
            DialogManager.showSuccess("Модель '" + modelType.getDisplayName() + "' добавлена в сцену");
        } catch (Exception e) {
            LOG.error("Ошибка добавления модели '{}': {}", modelType.getDisplayName(), e.getMessage(), e);
            DialogManager.showError("Ошибка добавления модели '" + modelType.getDisplayName() + "': " + e.getMessage());
        }
    }

    private void openModelAndAddToScene() {
        LOG.info("Открытие модели и добавление в сцену");

        getStage().ifPresentOrElse(
            stage -> {
                Optional<File> fileOptional = DialogManager.showOpenModelDialog(stage);
                fileOptional.ifPresent(file -> {
                    try {
                        String filePath = file.getAbsolutePath();
                        SceneObject newObject = sceneService.addModelToScene(currentScene, filePath);
                        currentScene.selectObject(newObject);
                        updateUIFromScene();
                        recentFilesCacheService.addFile(filePath);
                        updateRecentFilesMenu();
                        DialogManager.showSuccess("Модель добавлена в сцену: " + file.getName());
                    } catch (Exception e) {
                        LOG.error("Ошибка добавления модели: {}", e.getMessage(), e);
                        DialogManager.showError("Ошибка добавления модели: " + e.getMessage());
                    }
                });
            },
            () -> DialogManager.showError("Не удалось определить окно приложения")
        );
    }

    private void saveSelectedModelToFile() {
        if (currentScene.getSelectedObject() == null) {
            DialogManager.showError(MessageConstants.NO_MODEL_TO_SAVE);
            return;
        }

        Model modelToSave = currentScene.getSelectedObject().getModel();
        if (modelToSave == null) {
            DialogManager.showError("У выбранного объекта нет модели");
            return;
        }

        getStage().ifPresentOrElse(
            stage -> {
                Optional<File> fileOptional = DialogManager.showSaveModelDialog(stage);
                fileOptional.ifPresent(file -> {
                    try {
                        String filePath = file.getAbsolutePath();
                        modelService.saveModelToFile(modelToSave, filePath);
                        recentFilesCacheService.addFile(filePath);
                        updateRecentFilesMenu();
                        DialogManager.showSuccess(MessageConstants.OBJECT_SAVED + ": " + file.getName());
                    } catch (Exception e) {
                        LOG.error("Ошибка сохранения модели: {}", e.getMessage(), e);
                        DialogManager.showError(MessageConstants.OBJECT_SAVE_ERROR + ": " + e.getMessage());
                    }
                });
            },
            () -> DialogManager.showError("Не удалось определить окно приложения")
        );
    }

    private void registerCurrentStage() {
        getStage().ifPresent(WindowManager::registerStage);
    }

    private Optional<Stage> getStage() {
        return anchorPane != null && anchorPane.getScene() != null ?
            Optional.of((Stage) anchorPane.getScene().getWindow()) :
            Optional.empty();
    }

    private void handleExit() {
        saveRecentFilesToCache();

        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Подтверждение выхода",
            "Вы уверены, что хотите выйти из приложения?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            LOG.info("Пользователь подтвердил выход из приложения");
            Platform.exit();
        }
    }

    private void saveRecentFilesToCache() {
        List<String> recentFiles = recentFilesCacheService.getRecentFiles();
        CachePersistenceManager.saveRecentFiles(recentFiles);
    }

    private void applyTheme(String themePath) {
        LOG.debug("Применение темы: {}", themePath);

        URL themeUrl = getClass().getResource(themePath);
        if (themeUrl != null) {
            anchorPane.getStylesheets().clear();
            anchorPane.getStylesheets().add(themeUrl.toExternalForm());
            LOG.info("Тема '{}' применена успешно", themePath);
        } else {
            LOG.warn("Тема не найдена: {}", themePath);
        }
    }

    private void updateRecentFilesMenu() {
        menuRecent.getItems().removeIf(item -> !item.equals(menuRecentClear) && !(item instanceof SeparatorMenuItem));

        List<String> recentFiles = recentFilesCacheService.getRecentFiles();
        if (recentFiles.isEmpty()) {
            MenuItem emptyItem = new MenuItem("Нет недавних файлов");
            emptyItem.setDisable(true);
            menuRecent.getItems().add(0, emptyItem);
        } else {
            for (String filePath : recentFiles) {
                String fileName = new File(filePath).getName();
                MenuItem fileItem = new MenuItem(fileName);
                fileItem.setOnAction(event -> openRecentFile(filePath));
                menuRecent.getItems().add(0, fileItem);
            }
        }

        LOG.debug("Меню недавних файлов обновлено, файлов: {}", recentFiles.size());
    }

    private void openRecentFile(String filePath) {
        try {
            if (filePath.toLowerCase().endsWith(".3dscene")) {
                currentScene = sceneService.loadScene(filePath);
                updateUIFromScene();
                DialogManager.showSuccess("Сцена загружена: " + new File(filePath).getName());
            } else {
                SceneObject newObject = sceneService.addModelToScene(currentScene, filePath);
                currentScene.selectObject(newObject);
                updateUIFromScene();
                DialogManager.showSuccess("Модель добавлена в сцену: " + new File(filePath).getName());
            }
            recentFilesCacheService.addFile(filePath);
            updateRecentFilesMenu();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки недавнего файла '{}': {}", filePath, e.getMessage(), e);
            DialogManager.showError("Не удалось загрузить файл: " + new File(filePath).getName());
        }
    }

    private void clearRecentFiles() {
        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Очистка списка",
            "Вы уверены, что хотите очистить список недавних файлов?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            recentFilesCacheService.clearCache();
            updateRecentFilesMenu();
            CachePersistenceManager.saveRecentFiles(recentFilesCacheService.getRecentFiles());
            LOG.info("Список недавних файлов очищен пользователем");
        }
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                LOG.info("Открыт URL: {}", url);
            } else {
                LOG.warn("Браузер не поддерживается на этой платформе");
                DialogManager.showError("Не удалось открыть браузер: неподдерживаемая операция");
            }
        } catch (Exception e) {
            LOG.error("Ошибка открытия URL {}: {}", url, e.getMessage(), e);
            DialogManager.showError("Не удалось открыть браузер: " + e.getMessage());
        }
    }
}
