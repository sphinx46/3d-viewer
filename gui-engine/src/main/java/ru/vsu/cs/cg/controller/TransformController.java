package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import ru.vsu.cs.cg.exception.ApplicationException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.utils.controller.UiFieldUtils;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.tooltip.TooltipManager;
import ru.vsu.cs.cg.utils.validation.InputValidator;

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public class TransformController extends BaseController {

    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();

    @FXML private CheckBox visibilityCheckbox;
    @FXML private TextField objectNameField;
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
        try {
            initializeTooltips();
            initializeListeners();
            setFieldsEditable(false);
        } catch (Exception e) {
            LOG.error("Ошибка инициализации TransformController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, CONTROLLER_INIT_ERROR);
            throw new ApplicationException(CONTROLLER_INIT_ERROR + ": TransformController", e);
        }
    }

    private void initializeTooltips() {
        try {
            TooltipManager.addHotkeyTooltip(applyTransformButton, "applyTransformButton");
            TooltipManager.addHotkeyTooltip(resetTransformButton, "resetTransformButton");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации подсказок TransformController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    private void initializeListeners() {
        try {
            applyTransformButton.setOnAction(event -> handleApplyTransform());
            resetTransformButton.setOnAction(event -> handleResetTransform());

            objectNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (hasSelectedObject()) {
                        sceneController.getSelectedObject().setName(newValue);
                        LOG.debug("Имя объекта изменено на: {}", newValue);
                    }
                } catch (Exception e) {
                    LOG.error("Ошибка изменения имени объекта: {}", e.getMessage(), e);
                    EXCEPTION_HANDLER.handleException(e);
                }
            });

            visibilityCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (hasSelectedObject()) {
                        sceneController.getSelectedObject().setVisible(newValue);
                        LOG.debug("Видимость объекта изменена: {}", newValue);
                    }
                } catch (Exception e) {
                    LOG.error("Ошибка изменения видимости объекта: {}", e.getMessage(), e);
                    EXCEPTION_HANDLER.handleException(e);
                }
            });
        } catch (Exception e) {
            LOG.error("Ошибка инициализации слушателей TransformController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    private void handleApplyTransform() {
        try {
            if (!hasSelectedObject()) {
                DialogManager.showError("Нет выбранного объекта");
                return;
            }

            double[] transformValues = parseTransformValues();
            sceneController.applyTransformToSelectedObject(
                transformValues[0], transformValues[1], transformValues[2],
                transformValues[3], transformValues[4], transformValues[5],
                transformValues[6], transformValues[7], transformValues[8]
            );
            DialogManager.showInfo("Трансформация применена", "Параметры трансформации обновлены");
        } catch (ApplicationException e) {
            LOG.error("Ошибка применения трансформации: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TRANSFORMATION_ERROR);
            DialogManager.showError("Ошибка в данных трансформации");
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка применения трансформации: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TRANSFORMATION_ERROR);
            DialogManager.showError("Ошибка в данных трансформации");
            throw new ApplicationException(TRANSFORMATION_ERROR + ": применение трансформации", e);
        }
    }

    private void handleResetTransform() {
        try {
            if (!hasSelectedObject()) {
                DialogManager.showError("Нет выбранного объекта");
                return;
            }

            sceneController.resetTransformOfSelectedObject();
            DialogManager.showInfo("Трансформация сброшена", "Трансформация установлена по умолчанию");
        } catch (ApplicationException e) {
            LOG.error("Ошибка сброса трансформации: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TRANSFORMATION_ERROR);
            DialogManager.showError("Ошибка сброса трансформации");
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка сброса трансформации: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TRANSFORMATION_ERROR);
            DialogManager.showError("Ошибка сброса трансформации");
            throw new ApplicationException(TRANSFORMATION_ERROR + ": сброс трансформации", e);
        }
    }

    private double[] parseTransformValues() {
        try {
            return new double[] {
                InputValidator.parseDoubleSafe(positionX.getText(), 0.0),
                InputValidator.parseDoubleSafe(positionY.getText(), 0.0),
                InputValidator.parseDoubleSafe(positionZ.getText(), 0.0),
                InputValidator.parseDoubleSafe(rotationX.getText(), 0.0),
                InputValidator.parseDoubleSafe(rotationY.getText(), 0.0),
                InputValidator.parseDoubleSafe(rotationZ.getText(), 0.0),
                InputValidator.parseDoubleSafe(scaleX.getText(), 1.0),
                InputValidator.parseDoubleSafe(scaleY.getText(), 1.0),
                InputValidator.parseDoubleSafe(scaleZ.getText(), 1.0)
            };
        } catch (Exception e) {
            LOG.error("Ошибка парсинга значений трансформации: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TRANSFORMATION_ERROR);
            throw new ApplicationException(TRANSFORMATION_ERROR + ": парсинг значений", e);
        }
    }

    @Override
    protected void clearFields() {
        try {
            objectNameField.clear();
            visibilityCheckbox.setSelected(false);
            UiFieldUtils.clearTextFields(positionX, positionY, positionZ,
                rotationX, rotationY, rotationZ,
                scaleX, scaleY, scaleZ);
        } catch (Exception e) {
            LOG.error("Ошибка очистки полей TransformController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    @Override
    protected void populateFields(SceneObject object) {
        try {
            if (object == null) return;

            objectNameField.setText(object.getName());
            visibilityCheckbox.setSelected(object.isVisible());
            UiFieldUtils.setTextField(positionX, object.getTransform().getPositionX());
            UiFieldUtils.setTextField(positionY, object.getTransform().getPositionY());
            UiFieldUtils.setTextField(positionZ, object.getTransform().getPositionZ());
            UiFieldUtils.setTextField(rotationX, object.getTransform().getRotationX());
            UiFieldUtils.setTextField(rotationY, object.getTransform().getRotationY());
            UiFieldUtils.setTextField(rotationZ, object.getTransform().getRotationZ());
            UiFieldUtils.setTextField(scaleX, object.getTransform().getScaleX());
            UiFieldUtils.setTextField(scaleY, object.getTransform().getScaleY());
            UiFieldUtils.setTextField(scaleZ, object.getTransform().getScaleZ());
        } catch (Exception e) {
            LOG.error("Ошибка заполнения полей TransformController для объекта '{}': {}",
                object != null ? object.getName() : "null", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    @Override
    protected void setFieldsEditable(boolean editable) {
        try {
            objectNameField.setEditable(editable);
            visibilityCheckbox.setDisable(!editable);
            UiFieldUtils.setTextFieldsEditable(editable, positionX, positionY, positionZ,
                rotationX, rotationY, rotationZ,
                scaleX, scaleY, scaleZ);
            applyTransformButton.setDisable(!editable);
            resetTransformButton.setDisable(!editable);
        } catch (Exception e) {
            LOG.error("Ошибка установки редактируемости полей TransformController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }
}
