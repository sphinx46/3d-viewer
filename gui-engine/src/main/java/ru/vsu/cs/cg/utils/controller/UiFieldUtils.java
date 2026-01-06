package ru.vsu.cs.cg.utils.controller;

import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UiFieldUtils {

    private static final Logger LOG = LoggerFactory.getLogger(UiFieldUtils.class);

    private UiFieldUtils() {
    }

    public static void setTextField(TextField field, double value) {
        if (field != null) {
            field.setText(formatDouble(value));
        }
    }

    public static void bindSliderToField(Slider slider, TextField field, double defaultValue, double min, double max) {
        try {
            slider.setMin(min);
            slider.setMax(max);

            field.textProperty().bindBidirectional(slider.valueProperty(),
                new NumberStringConverter() {
                    @Override
                    public String toString(Number value) {
                        return String.format("%.2f", value.doubleValue());
                    }

                    @Override
                    public Number fromString(String string) {
                        try {
                            double val = Double.parseDouble(string);
                            return Math.max(min, Math.min(max, val));
                        } catch (NumberFormatException e) {
                            LOG.warn("Некорректное значение слайдера: {}, установлено значение по умолчанию: {}", string, defaultValue);
                            return defaultValue;
                        }
                    }
                });
            LOG.trace("Слайдер привязан к полю ввода, диапазон: [{}, {}]", min, max);
        } catch (Exception e) {
            LOG.error("Ошибка привязки слайдера к полю ввода: {}", e.getMessage());
        }
    }

    public static String formatDouble(double value) {
        try {
            return String.format("%.3f", value);
        } catch (Exception e) {
            LOG.error("Ошибка форматирования числа '{}': {}", value, e.getMessage());
            return String.valueOf(value);
        }
    }

    public static void clearTextFields(TextField... fields) {
        try {
            for (TextField field : fields) {
                if (field != null) {
                    field.clear();
                }
            }
            LOG.trace("Очищено {} текстовых полей", fields.length);
        } catch (Exception e) {
            LOG.error("Ошибка очистки текстовых полей: {}", e.getMessage());
        }
    }

    public static void setTextFieldsEditable(boolean editable, TextField... fields) {
        try {
            for (TextField field : fields) {
                if (field != null) {
                    field.setEditable(editable);
                }
            }
            LOG.trace("Установлена возможность редактирования {} текстовых полей: {}", fields.length, editable);
        } catch (Exception e) {
            LOG.error("Ошибка установки возможности редактирования текстовых полей: {}", e.getMessage());
        }
    }
}
