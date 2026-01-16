package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.scene.Material;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.utils.controller.UiFieldUtils;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;
import ru.vsu.cs.cg.utils.validation.InputValidator;

import java.io.File;
import java.util.Locale;
import java.util.function.Consumer;

public class MaterialController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(MaterialController.class);
    private static final double DEFAULT_LIGHT_INTENSITY = 1;
    private static final double DEFAULT_AMBIENT = 0.3;
    private static final double DEFAULT_DIFFUSE = 1;

    @FXML private ColorPicker colorPicker;
    @FXML private TextField materialLightIntensityField;
    @FXML private TextField materialDiffusionField;
    @FXML private TextField materialAmbientField;
    @FXML private Button loadTextureButton;
    @FXML private Button clearTextureButton;
    @FXML private CheckBox showTextureCheckbox;
    @FXML private CheckBox showLightingCheckbox;
    @FXML private CheckBox showPolygonalGridCheckbox;

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
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) ->
            updateMaterial(m -> m.setColor(newValue)));

        setupNumericField(materialLightIntensityField, 0.0, 5.0, val ->
                updateMaterial(m -> m.setLightIntensity(val)));

        setupNumericField(materialDiffusionField, 0.0, 3.0, val ->
                updateMaterial(m -> m.setAmbient(val)));

        setupNumericField(materialAmbientField, 0.0, 1.0, val ->
                updateMaterial(m -> m.setDiffusion(val)));

        showTextureCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
            updateRenderSettings(s -> s.setUseTexture(newValue)));

        showLightingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateRenderSettings(s -> s.setUseLighting(newValue));
            if (hasSelectedObject()) {
                sceneController.markModelModified();
            }
        });

        showPolygonalGridCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateRenderSettings(s -> s.setDrawPolygonalGrid(newValue));
            if (hasSelectedObject()) {
                sceneController.markModelModified();
            }
        });
    }

    private void setupNumericField(TextField field, double min, double max, Consumer<Double> valueSetter) {
        Runnable applyValue = () -> {
            if (!hasSelectedObject()) return;

            String text = field.getText();
            try {
                text = text.replace(',', '.');

                double val = Double.parseDouble(text);

                if (val < min) val = min;
                if (val > max) val = max;

                valueSetter.accept(val);

                field.setText(String.format(Locale.US, "%.2f", val));

            } catch (NumberFormatException e) {
                SceneObject obj = getSelectedObject();
                if (obj != null) populateFields(obj);
            }
        };

        field.setOnAction(event -> {
            applyValue.run();
            field.getParent().requestFocus();
        });

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                applyValue.run();
            }
        });
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
            new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
            new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(loadTextureButton.getScene().getWindow());

        if (selectedFile != null && selectedFile.exists()) {
            try {
                SceneObject selectedObject = getSelectedObject();
                Material currentMaterial = selectedObject.getMaterial();

                selectedObject.setMaterial(new Material(
                    currentMaterial.getRed(),
                    currentMaterial.getGreen(),
                    currentMaterial.getBlue(),
                    currentMaterial.getAlpha(),
                    selectedFile.getAbsolutePath(),
                    currentMaterial.getLightIntensity(),
                    currentMaterial.getDiffusion(),
                    currentMaterial.getAmbient()
                ));

                selectedObject.getRenderSettings().setUseTexture(true);
                showTextureCheckbox.setSelected(true);
                sceneController.markModelModified();

                LOG.info("Текстура успешно загружена: {}", selectedFile.getAbsolutePath());

            } catch (Exception e) {
                LOG.error("Ошибка при загрузке текстуры", e);
                DialogManager.showError("Ошибка загрузки текстуры: " + e.getMessage());
            }
        }
    }

    private void clearTexture() {
        if (!hasSelectedObject()) {
            return;
        }

        SceneObject selectedObject = getSelectedObject();
        Material currentMaterial = selectedObject.getMaterial();

        selectedObject.setMaterial(new Material(
            currentMaterial.getRed(),
            currentMaterial.getGreen(),
            currentMaterial.getBlue(),
            currentMaterial.getAlpha(),
            null,
            currentMaterial.getLightIntensity(),
            currentMaterial.getDiffusion(),
            currentMaterial.getAmbient()
        ));

        selectedObject.getRenderSettings().setUseTexture(false);
        showTextureCheckbox.setSelected(false);
        sceneController.markModelModified();

        LOG.info("Текстура удалена для объекта '{}'", selectedObject.getName());
    }

    private void updateMaterial(Consumer<Material> updater) {
        if (hasSelectedObject()) {
            updater.accept(getSelectedObject().getMaterial());
            sceneController.markModelModified();
        }
    }

    private void updateRenderSettings(Consumer<RasterizerSettings> updater) {
        if (hasSelectedObject()) {
            updater.accept(getSelectedObject().getRenderSettings());
            sceneController.markModelModified();
        }
    }

    @Override
    protected void clearFields() {
        colorPicker.setValue(Color.WHITE);
        materialLightIntensityField.setText(UiFieldUtils.formatDouble(DEFAULT_LIGHT_INTENSITY));
        materialAmbientField.setText(UiFieldUtils.formatDouble(DEFAULT_AMBIENT));
        materialDiffusionField.setText(UiFieldUtils.formatDouble(DEFAULT_DIFFUSE));
        showTextureCheckbox.setSelected(false);
        showLightingCheckbox.setSelected(false);
        showPolygonalGridCheckbox.setSelected(false);
    }

    @Override
    protected void populateFields(SceneObject object) {
        if (object == null) return;

        colorPicker.setValue(object.getMaterial().getColor());

        materialLightIntensityField.setText(String.format(Locale.US, "%.2f", object.getMaterial().getLightIntensity()));
        materialAmbientField.setText(String.format(Locale.US, "%.2f", object.getMaterial().getDiffusion()));
        materialDiffusionField.setText(String.format(Locale.US, "%.2f", object.getMaterial().getAmbient()));

        RasterizerSettings settings = object.getRenderSettings();
        showTextureCheckbox.setSelected(settings.isUseTexture());
        showLightingCheckbox.setSelected(settings.isUseLighting());
        showPolygonalGridCheckbox.setSelected(settings.isDrawPolygonalGrid());
    }

    @Override
    protected void setFieldsEditable(boolean editable) {
        colorPicker.setDisable(!editable);
        UiFieldUtils.setTextFieldsEditable(editable,
                materialLightIntensityField, materialAmbientField, materialDiffusionField);
        loadTextureButton.setDisable(!editable);
        clearTextureButton.setDisable(!editable);
        showTextureCheckbox.setDisable(!editable);
        showLightingCheckbox.setDisable(!editable);
        showPolygonalGridCheckbox.setDisable(!editable);
    }
}
