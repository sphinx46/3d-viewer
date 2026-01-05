package ru.vsu.cs.cg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.service.impl.ModelServiceImpl;
import ru.vsu.cs.cg.utils.DefaultModelLoader;
import ru.vsu.cs.cg.utils.DialogManager;
import ru.vsu.cs.cg.utils.MessageConstants;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuiController {

    private static final Logger LOG = LoggerFactory.getLogger(GuiController.class);
    private static final String DARK_THEME = "/static/css/theme-dark.css";
    private static final String LIGHT_THEME = "/static/css/theme-light.css";

    private final ModelService modelService = new ModelServiceImpl();
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
    @FXML private MenuItem menuCreatePlane;
    @FXML private MenuItem menuCreateCube;
    @FXML private MenuItem menuCreateCone;
    @FXML private MenuItem menuCreateCylinder;
    @FXML private MenuItem menuCreateTeapot;

    private final Map<Slider, TextField> sliderBindings = new HashMap<>();

    @FXML
    private void initialize() {
        LOG.info("Инициализация GuiController");

        try {
            menuThemeDark.setOnAction(event -> applyTheme(DARK_THEME));
            menuThemeLight.setOnAction(event -> applyTheme(LIGHT_THEME));

            menuFileNewCustom.setOnAction(event -> createCustomObject());
            menuFileSaveAs.setOnAction(event -> saveModelToFile());

            menuCreateCube.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CUBE));
            menuCreateCone.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CONE));
            menuCreateCylinder.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CYLINDER));
            menuCreateTeapot.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.TEAPOT));
            menuCreatePlane.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.PLANE));

            collectSliderBindings();
            setupSliderBindings();

            LOG.debug("GuiController успешно инициализирован");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации GuiController", e);
            throw e;
        }
    }

    private void createCustomObject() {
        LOG.info("Обработка создания пользовательского объекта");

        try {
            currentModel = modelService.createCustomObject();
            DialogManager.showInfo(MessageConstants.EMPTY_OBJECT_CREATED,
                MessageConstants.EMPTY_OBJECT_DETAILS);
        } catch (Exception e) {
            LOG.error("Ошибка создания пользовательского объекта: {}", e.getMessage(), e);
            DialogManager.showError("Ошибка создания пользовательского объекта: " + e.getMessage());
        }
    }

    private void createDefaultModel(DefaultModelLoader.ModelType modelType) {
        LOG.info("Обработка создания стандартной модели: {}", modelType.getDisplayName());

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

        Stage stage = (Stage) anchorPane.getScene().getWindow();
        Optional<File> fileOptional = DialogManager.showSaveDialog(stage);

        fileOptional.ifPresent(file -> {
            try {
                String filePath = file.getAbsolutePath();
                modelService.saveModelToFile(currentModel, filePath);
                DialogManager.showSuccess(MessageConstants.OBJECT_SAVED + ": " + file.getName());
            } catch (Exception e) {
                DialogManager.showError(MessageConstants.OBJECT_SAVE_ERROR + ": " + e.getMessage());
            }
        });
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

    private void collectSliderBindings() {
        sliderBindings.put(brightnessSlider, brightnessField);
        sliderBindings.put(reflectionSlider, reflectionField);
        sliderBindings.put(focalLengthSlider, focalLengthField);
        sliderBindings.put(sensitivitySlider, sensitivityField);

        LOG.debug("Собрано {} привязок слайдеров к текстовым полям", sliderBindings.size());
    }

    private void setupSliderBindings() {
        sliderBindings.forEach((slider, textField) -> {
            StringConverter<Number> converter = new NumberStringConverter() {
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
                        double min = slider.getMin();
                        double max = slider.getMax();
                        return Math.max(min, Math.min(value, max));
                    } catch (NumberFormatException e) {
                        LOG.warn("Неверный формат числа: '{}'", string);
                        return slider.getValue();
                    }
                }
            };

            textField.textProperty().bindBidirectional(slider.valueProperty(), converter);
        });

        LOG.debug("Привязки слайдеров настроены");
    }
}
