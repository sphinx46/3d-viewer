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
import java.util.function.Consumer;

public class MaterialController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(MaterialController.class);
    private static final double DEFAULT_SHININESS = 0.5;
    private static final double DEFAULT_REFLECTIVITY = 0.0;
    private static final double DEFAULT_TRANSPARENCY = 0.0;

    @FXML private ColorPicker colorPicker;
    @FXML private TextField materialShininessField;
    @FXML private TextField materialTransparencyField;
    @FXML private TextField materialReflectivityField;
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

        materialShininessField.textProperty().addListener((observable, oldValue, newValue) ->
            updateMaterial(m -> m.setShininess(parseAndClamp(newValue, DEFAULT_SHININESS))));

        materialTransparencyField.textProperty().addListener((observable, oldValue, newValue) ->
            updateMaterial(m -> m.setTransparency(parseAndClamp(newValue, DEFAULT_TRANSPARENCY))));

        materialReflectivityField.textProperty().addListener((observable, oldValue, newValue) ->
            updateMaterial(m -> m.setReflectivity(parseAndClamp(newValue, DEFAULT_REFLECTIVITY))));

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

    private double parseAndClamp(String value, double defaultValue) {
        return InputValidator.clamp(InputValidator.parseDoubleSafe(value, defaultValue), 0.0, 1.0);
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
                    currentMaterial.getShininess(),
                    currentMaterial.getReflectivity(),
                    currentMaterial.getTransparency()
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
            currentMaterial.getShininess(),
            currentMaterial.getReflectivity(),
            currentMaterial.getTransparency()
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
        materialShininessField.setText(UiFieldUtils.formatDouble(DEFAULT_SHININESS));
        materialReflectivityField.setText(UiFieldUtils.formatDouble(DEFAULT_REFLECTIVITY));
        materialTransparencyField.setText(UiFieldUtils.formatDouble(DEFAULT_TRANSPARENCY));
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

        RasterizerSettings settings = object.getRenderSettings();
        showTextureCheckbox.setSelected(settings.isUseTexture());
        showLightingCheckbox.setSelected(settings.isUseLighting());
        showPolygonalGridCheckbox.setSelected(settings.isDrawPolygonalGrid());
    }

    @Override
    protected void setFieldsEditable(boolean editable) {
        colorPicker.setDisable(!editable);
        UiFieldUtils.setTextFieldsEditable(editable,
            materialShininessField, materialReflectivityField, materialTransparencyField);
        loadTextureButton.setDisable(!editable);
        clearTextureButton.setDisable(!editable);
        showTextureCheckbox.setDisable(!editable);
        showLightingCheckbox.setDisable(!editable);
        showPolygonalGridCheckbox.setDisable(!editable);
    }
}
