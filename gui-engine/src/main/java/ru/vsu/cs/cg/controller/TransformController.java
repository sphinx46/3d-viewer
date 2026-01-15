package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.utils.controller.UiFieldUtils;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;
import ru.vsu.cs.cg.utils.validation.InputValidator;

public class TransformController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(TransformController.class);

    @FXML private TextField positionX;
    @FXML private TextField positionY;
    @FXML private TextField positionZ;
    @FXML private TextField rotationX;
    @FXML private TextField rotationY;
    @FXML private TextField rotationZ;
    @FXML private TextField scaleX;
    @FXML private TextField scaleY;
    @FXML private TextField scaleZ;
    @FXML private Button applyTransformButton;
    @FXML private Button resetTransformButton;

    @FXML
    private void initialize() {
        LOG.info("Инициализация TransformController");
        initializeTooltips();
        initializeListeners();
        setFieldsEditable(false);
    }

    private void initializeTooltips() {
        TooltipManager.addHotkeyTooltip(applyTransformButton, "applyTransformButton");
        TooltipManager.addHotkeyTooltip(resetTransformButton, "resetTransformButton");
    }

    private void initializeListeners() {
        applyTransformButton.setOnAction(event -> handleApplyTransform());
        resetTransformButton.setOnAction(event -> handleResetTransform());
    }

    private void handleApplyTransform() {
        if (!hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        try {
            double[] transformValues = parseTransformValues();
            sceneController.applyTransformToSelectedObject(
                transformValues[0], transformValues[1], transformValues[2],
                transformValues[3], transformValues[4], transformValues[5],
                transformValues[6], transformValues[7], transformValues[8]
            );
            DialogManager.showInfo("Трансформация применена", "Параметры трансформации обновлены");
        } catch (Exception e) {
            LOG.error("Ошибка применения трансформации: {}", e.getMessage());
            DialogManager.showError("Ошибка в данных трансформации");
        }
    }

    private void handleResetTransform() {
        if (!hasSelectedObject()) {
            DialogManager.showError("Нет выбранного объекта");
            return;
        }

        sceneController.resetTransformOfSelectedObject();
        DialogManager.showInfo("Трансформация сброшена", "Трансформация установлена по умолчанию");
    }

    private double[] parseTransformValues() {
        return new double[] {
            InputValidator.parseDoubleSafe(positionX.getText(), 0.0),
            InputValidator.parseDoubleSafe(positionY.getText(), 0.0),
            InputValidator.parseDoubleSafe(positionZ.getText(), 0.0),
            Math.toRadians(InputValidator.parseDoubleSafe(rotationX.getText(), 0.0)),
            Math.toRadians(InputValidator.parseDoubleSafe(rotationY.getText(), 0.0)),
            Math.toRadians(InputValidator.parseDoubleSafe(rotationZ.getText(), 0.0)),
            InputValidator.parseDoubleSafe(scaleX.getText(), 1.0),
            InputValidator.parseDoubleSafe(scaleY.getText(), 1.0),
            InputValidator.parseDoubleSafe(scaleZ.getText(), 1.0)
        };
    }

    @Override
    protected void clearFields() {
        UiFieldUtils.clearTextFields(positionX, positionY, positionZ,
            rotationX, rotationY, rotationZ,
            scaleX, scaleY, scaleZ);
    }

    @Override
    protected void populateFields(SceneObject object) {
        if (object == null) return;

        UiFieldUtils.setTextField(positionX, object.getTransform().getPositionX());
        UiFieldUtils.setTextField(positionY, object.getTransform().getPositionY());
        UiFieldUtils.setTextField(positionZ, object.getTransform().getPositionZ());
        UiFieldUtils.setTextField(rotationX, Math.toDegrees(object.getTransform().getRotationX()));
        UiFieldUtils.setTextField(rotationY, Math.toDegrees(object.getTransform().getRotationY()));
        UiFieldUtils.setTextField(rotationZ, Math.toDegrees(object.getTransform().getRotationZ()));
        UiFieldUtils.setTextField(scaleX, object.getTransform().getScaleX());
        UiFieldUtils.setTextField(scaleY, object.getTransform().getScaleY());
        UiFieldUtils.setTextField(scaleZ, object.getTransform().getScaleZ());
    }

    @Override
    protected void setFieldsEditable(boolean editable) {
        UiFieldUtils.setTextFieldsEditable(editable, positionX, positionY, positionZ,
            rotationX, rotationY, rotationZ,
            scaleX, scaleY, scaleZ);
        applyTransformButton.setDisable(!editable);
        resetTransformButton.setDisable(!editable);
    }
}
