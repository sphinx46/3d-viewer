package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GuiController {

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

    @FXML private MenuItem menuFileNew;

    private final Map<Slider, TextField> sliderBindings = new HashMap<>();

    @FXML
    private void initialize() {
        setupThemeHandlers();
        collectSliderBindings();
        setupAllSliderBindings();
    }

    private void setupThemeHandlers() {
        menuThemeDark.setOnAction(event -> applyTheme(DARK_THEME));
        menuThemeLight.setOnAction(event -> applyTheme(LIGHT_THEME));
    }

    private void applyTheme(String themePath) {
        URL themeUrl = getClass().getResource(themePath);
        if (themeUrl != null) {
            anchorPane.getStylesheets().clear();
            anchorPane.getStylesheets().add(themeUrl.toExternalForm());
        }
    }

    private void collectSliderBindings() {
        sliderBindings.put(brightnessSlider, brightnessField);
        sliderBindings.put(reflectionSlider, reflectionField);
        sliderBindings.put(focalLengthSlider, focalLengthField);
        sliderBindings.put(sensitivitySlider, sensitivityField);
    }

    private void setupAllSliderBindings() {
        sliderBindings.forEach(this::bindSliderToTextField);
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
            return slider.getValue();
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
