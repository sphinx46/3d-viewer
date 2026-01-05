package ru.vsu.cs.cg.controller;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.service.impl.ModelServiceImpl;
import ru.vsu.cs.cg.utils.*;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class GuiController {

    private static final Logger LOG = LoggerFactory.getLogger(GuiController.class);
    private static final String DARK_THEME = "/static/css/theme-dark.css";
    private static final String LIGHT_THEME = "/static/css/theme-light.css";
    private static final String GITHUB_ISSUES_URL = "https://github.com/sphinx46/3d-viewer/issues/new";
    private static final String GITHUB_DOCS_URL = "https://github.com/sphinx46/3d-viewer";

    private final ModelService modelService = new ModelServiceImpl();
    private final Map<Slider, TextField> sliderBindings = new HashMap<>();
    private Model currentModel;

    @FXML private AnchorPane anchorPane;

    @FXML private MenuItem menuThemeDark;
    @FXML private MenuItem menuThemeLight;

    @FXML private Slider brightnessSlider;
    @FXML private TextField brightnessField;
    @FXML private Slider reflectionSlider;
    @FXML private TextField reflectionField;
    @FXML private Slider focalLengthSlider;
    @FXML private TextField focalLengthField;
    @FXML private Slider sensitivitySlider;
    @FXML private TextField sensitivityField;

    @FXML private MenuItem menuFileNewCustom;
    @FXML private MenuItem menuFileSaveAs;
    @FXML private MenuItem menuFileExit;

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
    @FXML private MenuItem menuHelpBugReport;
    @FXML private MenuItem menuHelpAbout;

    @FXML
    private void initialize() {
        LOG.info("Инициализация GuiController");

        try {
            initializeSliderBindings();
            initializeMenuActions();
            registerCurrentStage();
            LOG.debug("GuiController успешно инициализирован");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации GuiController", e);
            throw new RuntimeException("Ошибка инициализации контроллера", e);
        }
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

        menuFileNewCustom.setOnAction(event -> createCustomObject());
        menuFileSaveAs.setOnAction(event -> saveModelToFile());
        menuFileExit.setOnAction(event -> handleExit());

        menuCreatePlane.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.PLANE));
        menuCreateCube.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CUBE));
        menuCreateCone.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CONE));
        menuCreateCylinder.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CYLINDER));
        menuCreateTeapot.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.TEAPOT));

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
                "Программа для просмотра и редактирования 3D моделей.";
            DialogManager.showInfo("О программе", aboutText);
        });
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
        Optional<ButtonType> result = DialogManager.showConfirmation(
            "Подтверждение выхода",
            "Вы уверены, что хотите выйти из приложения?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            LOG.info("Пользователь подтвердил выход из приложения");
            Platform.exit();
        }
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

    private void createCustomObject() {
        LOG.info("Создание пользовательского объекта");

        try {
            currentModel = modelService.createCustomObject();
            DialogManager.showInfo(
                MessageConstants.EMPTY_OBJECT_CREATED,
                MessageConstants.EMPTY_OBJECT_DETAILS
            );
        } catch (Exception e) {
            LOG.error("Ошибка создания пользовательского объекта: {}", e.getMessage(), e);
            DialogManager.showError("Ошибка создания пользовательского объекта: " + e.getMessage());
        }
    }

    private void createDefaultModel(DefaultModelLoader.ModelType modelType) {
        LOG.info("Создание стандартной модели: {}", modelType.getDisplayName());

        try {
            currentModel = modelService.loadDefaultModel(modelType);
            DialogManager.showSuccess("Модель '" + modelType.getDisplayName() + "' успешно загружена");
        } catch (Exception e) {
            LOG.error("Ошибка загрузки модели '{}': {}", modelType.getDisplayName(), e.getMessage(), e);
            DialogManager.showError("Ошибка загрузки модели '" + modelType.getDisplayName() + "': " + e.getMessage());
        }
    }

    private void saveModelToFile() {
        if (currentModel == null) {
            DialogManager.showError(MessageConstants.NO_MODEL_TO_SAVE);
            return;
        }

        getStage().ifPresentOrElse(
            stage -> {
                Optional<File> fileOptional = DialogManager.showSaveModelDialog(stage);
                fileOptional.ifPresent(file -> {
                    try {
                        modelService.saveModelToFile(currentModel, file.getAbsolutePath());
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
