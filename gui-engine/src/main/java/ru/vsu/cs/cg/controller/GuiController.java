package ru.vsu.cs.cg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.utils.DefaultModelLoader;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GuiController {

    private static final Logger LOG = LoggerFactory.getLogger(GuiController.class);
    private static final String DARK_THEME = "/static/css/theme-dark.css";
    private static final String LIGHT_THEME = "/static/css/theme-light.css";

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
    @FXML private MenuItem menuCreatePlane;
    @FXML private MenuItem menuCreateCube;
    @FXML private MenuItem menuCreateCone;
    @FXML private MenuItem menuCreateCylinder;
    @FXML private MenuItem menuCreateSphere;
    @FXML private MenuItem menuCreateTeapot;

    private final Map<Slider, TextField> sliderBindings = new HashMap<>();

    @FXML
    private void initialize() {
        LOG.info("Инициализация GuiController");

        try {
            setupThemeHandlers();
            setupModelCreationHandlers();
            collectSliderBindings();
            setupAllSliderBindings();

            LOG.debug("GuiController успешно инициализирован");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации GuiController", e);
            throw e;
        }
    }

    private void setupThemeHandlers() {
        menuThemeDark.setOnAction(event -> applyTheme(DARK_THEME));
        menuThemeLight.setOnAction(event -> applyTheme(LIGHT_THEME));
    }

    private void setupModelCreationHandlers() {
        menuCreateCube.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CUBE));
        menuCreateCone.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CONE));
        menuCreateCylinder.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.CYLINDER));
        menuCreateTeapot.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.TEAPOT));
        menuCreatePlane.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.PLANE));
        menuCreateSphere.setOnAction(event -> createDefaultModel(DefaultModelLoader.ModelType.SPHERE));
    }

    private void createDefaultModel(DefaultModelLoader.ModelType modelType) {
        LOG.info("Создание модели: {}", modelType.getDisplayName());

        try {
            final Model model = DefaultModelLoader.loadModel(modelType);
            handleLoadedModel(model, modelType);
        } catch (Exception e) {
            LOG.error("Ошибка при создании модели '{}'", modelType.getDisplayName(), e);
            handleModelLoadError(e);
        }
    }

    private void handleLoadedModel(Model model, DefaultModelLoader.ModelType modelType) {
        final int vertexCount = model.getVertices().size();
        final int polygonCount = model.getPolygons().size();

        LOG.info("Модель '{}' обработана (вершин: {}, полигонов: {})",
            modelType.getDisplayName(), vertexCount, polygonCount);
    }

    private void handleModelLoadError(Exception e) {
        LOG.error("Обработка ошибки загрузки модели: {}", e.getMessage());
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

    private void setupAllSliderBindings() {
        sliderBindings.forEach(this::bindSliderToTextField);
        LOG.debug("Привязки слайдеров настроены");
    }

    private void bindSliderToTextField(Slider slider, TextField textField) {
        StringConverter<Number> converter = createConverterForSlider(slider);
        textField.textProperty().bindBidirectional(slider.valueProperty(), converter);
    }

    private StringConverter<Number> createConverterForSlider(Slider slider) {
        return new NumberStringConverter() {
            @Override
            public String toString(Number value) {
                return formatValue(value.doubleValue(), slider);
            }

            @Override
            public Number fromString(String string) {
                return parseValue(string, slider);
            }
        };
    }

    private String formatValue(double value, Slider slider) {
        return slider == focalLengthSlider ?
            String.format("%.0f", value) :
            String.format("%.2f", value);
    }

    private double parseValue(String input, Slider slider) {
        try {
            double value = Double.parseDouble(input.replace(',', '.'));
            return clamp(value, slider.getMin(), slider.getMax());
        } catch (NumberFormatException e) {
            LOG.warn("Неверный формат числа в слайдере: '{}'", input);
            return slider.getValue();
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
