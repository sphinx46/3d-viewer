package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import ru.vsu.cs.cg.scene.Material;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.utils.controller.UiFieldUtils;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;
import ru.vsu.cs.cg.utils.validation.InputValidator;

public class MaterialController extends BaseController {

    @FXML private ColorPicker colorPicker;
    @FXML private TextField materialShininessField;
    @FXML private TextField materialTransparencyField;
    @FXML private TextField materialReflectivityField;
    @FXML private Button loadTextureButton;
    @FXML private Button clearTextureButton;
    @FXML private CheckBox showTextureCheckbox;
    @FXML private CheckBox showLightingCheckbox;
    @FXML private Slider brightnessSlider;
    @FXML private TextField brightnessField;
    @FXML private Slider reflectionSlider;
    @FXML private TextField reflectionField;
    @FXML private Label materialColorLabel;
    @FXML private Label materialShininessLabel;
    @FXML private Label materialTransparencyLabel;
    @FXML private Label materialReflectivityLabel;
    @FXML private Label textureLabel;
    @FXML private Label brightnessLabel;
    @FXML private Label reflectionLabel;
    @FXML private Label lightingLabel;
    @FXML private Label displayLabel;

    @FXML
    private void initialize() {
        LOG.info("Инициализация MaterialController");
        initializeTooltips();
        initializeBindings();
        initializeButtonActions();
        setFieldsEditable(false);
        updateLabels();
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

        showTextureCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (hasSelectedObject()) {
                LOG.info("Изменение отображения текстуры: {}", newValue);
            }
        });

        showLightingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (hasSelectedObject()) {
                LOG.info("Изменение отображения освещения: {}", newValue);
            }
        });
    }

    private void initializeButtonActions() {
        loadTextureButton.setOnAction(event -> loadTexture());
        clearTextureButton.setOnAction(event -> clearTexture());
    }

    private void loadTexture() {
        if (!hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта для загрузки текстуры");
            return;
        }

        LOG.info("Загрузка текстуры для объекта '{}'", getSelectedObject().getName());
        DialogManager.showInfo("Загрузка текстуры", "Функция загрузки текстуры в разработке");
    }

    private void clearTexture() {
        if (!hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта для очистки текстуры");
            return;
        }

        getSelectedObject().getMaterial().setTexturePath(null);
        LOG.info("Текстура очищена для объекта '{}'", getSelectedObject().getName());
        DialogManager.showInfo("Очистка текстуры", "Текстура удалена из объекта");
    }

    @Override
    protected void clearFields() {
        colorPicker.setValue(Color.WHITE);
        materialShininessField.clear();
        materialReflectivityField.clear();
        materialTransparencyField.clear();
        brightnessSlider.setValue(0.5);
        reflectionSlider.setValue(0.3);
        showTextureCheckbox.setSelected(true);
        showLightingCheckbox.setSelected(true);
        updateLabels();
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
        showTextureCheckbox.setSelected(true);
        showLightingCheckbox.setSelected(true);
        updateLabels();
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
        brightnessSlider.setDisable(!editable);
        reflectionSlider.setDisable(!editable);
    }

    private void updateSelectedObjectMaterial(MaterialUpdater updater) {
        if (hasSelectedObject()) {
            updater.update(getSelectedObject().getMaterial());
        }
    }

    private void updateLabels() {
        boolean hasSelectedObject = hasSelectedObject();

        if (materialColorLabel != null) materialColorLabel.setDisable(!hasSelectedObject);
        if (materialShininessLabel != null) materialShininessLabel.setDisable(!hasSelectedObject);
        if (materialTransparencyLabel != null) materialTransparencyLabel.setDisable(!hasSelectedObject);
        if (materialReflectivityLabel != null) materialReflectivityLabel.setDisable(!hasSelectedObject);
        if (textureLabel != null) textureLabel.setDisable(!hasSelectedObject);
        if (lightingLabel != null) lightingLabel.setDisable(!hasSelectedObject);
        if (brightnessLabel != null) brightnessLabel.setDisable(!hasSelectedObject);
        if (reflectionLabel != null) reflectionLabel.setDisable(!hasSelectedObject);
        if (displayLabel != null) displayLabel.setDisable(!hasSelectedObject);
    }

    @FunctionalInterface
    private interface MaterialUpdater {
        void update(Material material);
    }
}
