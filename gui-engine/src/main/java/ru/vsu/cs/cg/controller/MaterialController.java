package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.rasterization.Texture;
import ru.vsu.cs.cg.scene.Material;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.utils.controller.UiFieldUtils;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;
import ru.vsu.cs.cg.utils.validation.InputValidator;

import java.io.File;

public class MaterialController extends BaseController {

    @FXML private ColorPicker colorPicker;
    @FXML private TextField materialShininessField;
    @FXML private TextField materialTransparencyField;
    @FXML private TextField materialReflectivityField;
    @FXML private Button loadTextureButton;
    @FXML private Button clearTextureButton;
    @FXML private CheckBox showTextureCheckbox;
    @FXML private CheckBox showLightingCheckbox;
    @FXML private CheckBox showPolygonalGridCheckbox;
    @FXML private Slider brightnessSlider;
    @FXML private TextField brightnessField;
    @FXML private Slider reflectionSlider;
    @FXML private TextField reflectionField;

    @FXML
    private void initialize() {
        LOG.info("Инициализация MaterialController");
        initializeTooltips();
        initializeBindings();
        initializeButtonActions();
        setFieldsEditable(false);
    }

    private void initializeTooltips() {
        TooltipManager.addHotkeyTooltip(loadTextureButton, "loadTextureButton");
        TooltipManager.addHotkeyTooltip(clearTextureButton, "clearTextureButton");
    }

    private void initializeBindings() {
        UiFieldUtils.bindSliderToField(brightnessSlider, brightnessField, 0.5, 0.0, 1.0);
        UiFieldUtils.bindSliderToField(reflectionSlider, reflectionField, 0.3, 0.0, 1.0);

        colorPicker.valueProperty().addListener((observable, oldValue, newValue) ->
            updateSelectedObjectMaterial(material -> material.setColor(newValue)));

        materialShininessField.textProperty().addListener((observable, oldValue, newValue) ->
            updateSelectedObjectMaterial(material -> {
                double value = InputValidator.parseDoubleSafe(newValue, 0.5);
                material.setShininess(InputValidator.clamp(value, 0.0, 1.0));
            }));

        materialTransparencyField.textProperty().addListener((observable, oldValue, newValue) ->
            updateSelectedObjectMaterial(material -> {
                double value = InputValidator.parseDoubleSafe(newValue, 0.0);
                material.setTransparency(InputValidator.clamp(value, 0.0, 1.0));
            }));

        materialReflectivityField.textProperty().addListener((observable, oldValue, newValue) ->
            updateSelectedObjectMaterial(material -> {
                double value = InputValidator.parseDoubleSafe(newValue, 0.0);
                material.setReflectivity(InputValidator.clamp(value, 0.0, 1.0));
            }));

        showTextureCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
            updateSelectedObjectRenderSettings(settings -> settings.setUseTexture(newValue)));

        showLightingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
            updateSelectedObjectRenderSettings(settings -> settings.setUseLighting(newValue)));

        showPolygonalGridCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
            updateSelectedObjectRenderSettings(settings -> settings.setDrawPolygonalGrid(newValue)));
    }

    private void initializeButtonActions() {
        loadTextureButton.setOnAction(event -> loadTexture());
        clearTextureButton.setOnAction(event -> clearTexture());
    }

    private void loadTexture() {
        if (!hasSelectedObject()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл текстуры");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
            new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(loadTextureButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());

                if (image.isError()) {
                    throw new Exception("Не удалось прочитать формат изображения");
                }

                Texture newTexture = new Texture(image);
                SceneObject selectedObject = getSelectedObject();

                selectedObject.setTexture(newTexture);
                selectedObject.getMaterial().setTexturePath(selectedFile.getAbsolutePath());
                selectedObject.getRenderSettings().setUseTexture(true);
                showTextureCheckbox.setSelected(true);

                LOG.info("Текстура успешно загружена: {}", selectedFile.getName());

            } catch (Exception e) {
                LOG.error("Ошибка при загрузке текстуры", e);
            }
        }
    }

    private void clearTexture() {
        if (!hasSelectedObject()) {
            return;
        }

        SceneObject selectedObject = getSelectedObject();

        selectedObject.setTexture(null);
        selectedObject.getMaterial().setTexturePath(null);
        selectedObject.getRenderSettings().setUseTexture(false);
        showTextureCheckbox.setSelected(false);

        LOG.info("Текстура удалена для объекта '{}'", selectedObject.getName());
    }

    @Override
    protected void clearFields() {
        colorPicker.setValue(Color.WHITE);
        materialShininessField.clear();
        materialReflectivityField.clear();
        materialTransparencyField.clear();
        brightnessSlider.setValue(0.5);
        reflectionSlider.setValue(0.3);
        showTextureCheckbox.setSelected(false);
        showLightingCheckbox.setSelected(false);
        showPolygonalGridCheckbox.setSelected(false);
    }

    @Override
    protected void populateFields(SceneObject object) {
        if (object == null) return;

        colorPicker.setValue(object.getMaterial().getColor());
        materialShininessField.setText(UiFieldUtils.formatDouble(object.getMaterial().getShininess()));
        materialReflectivityField.setText(UiFieldUtils.formatDouble(object.getMaterial().getReflectivity()));
        materialTransparencyField.setText(UiFieldUtils.formatDouble(object.getMaterial().getTransparency()));
        brightnessSlider.setValue(0.5);
        reflectionSlider.setValue(0.3);

        RasterizerSettings settings = object.getRenderSettings();
        showTextureCheckbox.setSelected(settings.isUseTexture());
        showLightingCheckbox.setSelected(settings.isUseLighting());
        showPolygonalGridCheckbox.setSelected(settings.isDrawPolygonalGrid());
    }

    @Override
    protected void setFieldsEditable(boolean editable) {
        colorPicker.setDisable(!editable);
        materialShininessField.setEditable(editable);
        materialReflectivityField.setEditable(editable);
        materialTransparencyField.setEditable(editable);
        loadTextureButton.setDisable(!editable);
        clearTextureButton.setDisable(!editable);
        showTextureCheckbox.setDisable(!editable);
        showLightingCheckbox.setDisable(!editable);
        showPolygonalGridCheckbox.setDisable(!editable);
        brightnessSlider.setDisable(!editable);
        reflectionSlider.setDisable(!editable);
    }

    private void updateSelectedObjectMaterial(MaterialUpdater updater) {
        if (hasSelectedObject()) {
            updater.update(getSelectedObject().getMaterial());
        }
    }

    private void updateSelectedObjectRenderSettings(RenderSettingsUpdater updater) {
        if (hasSelectedObject()) {
            updater.update(getSelectedObject().getRenderSettings());
        }
    }

    @FunctionalInterface
    private interface MaterialUpdater {
        void update(Material material);
    }

    @FunctionalInterface
    private interface RenderSettingsUpdater {
        void update(RasterizerSettings settings);
    }
}
