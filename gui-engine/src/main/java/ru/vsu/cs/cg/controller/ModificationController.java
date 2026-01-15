package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.model.selection.ModelSelection;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.utils.controller.UiFieldUtils;
import ru.vsu.cs.cg.utils.parser.IndexParser;
import ru.vsu.cs.cg.utils.modification.ModificationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModificationController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(ModificationController.class);

    @FXML private TextField vertexIndicesField;
    @FXML private CheckBox cleanUnusedCheckbox;
    @FXML private Button removeVerticesButton;
    @FXML private Button selectVerticesButton;
    @FXML private Button deselectVerticesButton;

    @FXML private TextField polygonIndicesField;
    @FXML private Button removePolygonsButton;
    @FXML private Button selectPolygonsButton;
    @FXML private Button deselectPolygonsButton;

    @FXML private Label vertexCountLabel;
    @FXML private Label polygonCountLabel;
    @FXML private Label textureCountLabel;
    @FXML private Label normalCountLabel;

    @FXML
    private void initialize() {
        setupButtonActions();
        setInitialState();
    }

    private void setInitialState() {
        clearFields();
        setFieldsEditable(false);
    }

    private void setupButtonActions() {
        removeVerticesButton.setOnAction(event -> handleRemoveVertices());
        removePolygonsButton.setOnAction(event -> handleRemovePolygons());
        selectVerticesButton.setOnAction(event -> handleSelectVertices());
        selectPolygonsButton.setOnAction(event -> handleSelectPolygons());
        deselectVerticesButton.setOnAction(event -> handleDeselectVertices());
        deselectPolygonsButton.setOnAction(event -> handleDeselectPolygons());
    }

    private void handleRemoveVertices() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка удалить вершины без выбранного объекта");
            return;
        }

        String indicesInput = vertexIndicesField.getText();
        boolean clearUnused = cleanUnusedCheckbox.isSelected();

        ru.vsu.cs.cg.controller.command.Command command =
            new ru.vsu.cs.cg.controller.command.impl.modification.RemoveVerticesCommand(
                sceneController, indicesInput, clearUnused);
        command.execute();

        updateStatistics();
        LOG.debug("Команда удаления вершин выполнена");
    }

    private void handleRemovePolygons() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка удалить полигоны без выбранного объекта");
            return;
        }

        String indicesInput = polygonIndicesField.getText();

        ru.vsu.cs.cg.controller.command.Command command =
            new ru.vsu.cs.cg.controller.command.impl.modification.RemovePolygonsCommand(
                sceneController, indicesInput);
        command.execute();

        updateStatistics();
        LOG.debug("Команда удаления полигонов выполнена");
    }

    private void handleSelectVertices() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка выделить вершины без выбранного объекта");
            return;
        }

        SceneObject selectedObject = getSelectedObject();
        Model model = selectedObject.getModel();
        String indicesInput = vertexIndicesField.getText();

        ModificationUtils.updateSelection(indicesInput, model, true, true);
        updateSelectionFields();
        sceneController.markModelModified();
        sceneController.markSceneModified();
    }

    private void handleSelectPolygons() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка выделить полигоны без выбранного объекта");
            return;
        }

        SceneObject selectedObject = getSelectedObject();
        Model model = selectedObject.getModel();
        String indicesInput = polygonIndicesField.getText();

        ModificationUtils.updateSelection(indicesInput, model, false, true);
        updateSelectionFields();
        sceneController.markModelModified();
        sceneController.markSceneModified();
    }

    private void handleDeselectVertices() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка снять выделение вершин без выбранного объекта");
            return;
        }

        SceneObject selectedObject = getSelectedObject();
        Model model = selectedObject.getModel();
        String indicesInput = vertexIndicesField.getText();

        ModificationUtils.updateSelection(indicesInput, model, true, false);
        updateSelectionFields();
        sceneController.markModelModified();
        sceneController.markSceneModified();
    }

    private void handleDeselectPolygons() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка снять выделение полигонов без выбранного объекта");
            return;
        }

        SceneObject selectedObject = getSelectedObject();
        Model model = selectedObject.getModel();
        String indicesInput = polygonIndicesField.getText();

        ModificationUtils.updateSelection(indicesInput, model, false, false);
        updateSelectionFields();
        sceneController.markModelModified();
        sceneController.markSceneModified();
    }

    @Override
    protected void clearFields() {
        Platform.runLater(() -> {
            UiFieldUtils.clearTextFields(vertexIndicesField, polygonIndicesField);
            cleanUnusedCheckbox.setSelected(false);
            resetStatistics();
        });
    }

    @Override
    protected void populateFields(SceneObject object) {
        updateStatistics();
        updateSelectionFields();
    }

    private void updateSelectionFields() {
        if (!hasSelectedObject()) return;

        SceneObject selectedObject = getSelectedObject();
        Model model = selectedObject.getModel();
        ModelSelection selection = model.getSelection();

        Platform.runLater(() -> {
            if (selection.hasSelectedVertices()) {
                List<Integer> vertices = new ArrayList<>(selection.getSelectedVertices());
                vertexIndicesField.setText(IndexParser.formatIndices(vertices));
            }

            if (selection.hasSelectedPolygons()) {
                List<Integer> polygons = new ArrayList<>(selection.getSelectedPolygons());
                polygonIndicesField.setText(IndexParser.formatIndices(polygons));
            }
        });
    }

    public void updateStatistics() {
        Platform.runLater(() -> {
            if (!hasSelectedObject()) {
                resetStatistics();
                return;
            }

            SceneObject selectedObject = getSelectedObject();
            if (selectedObject == null) {
                resetStatistics();
                return;
            }

            Model model = selectedObject.getModel();
            if (model == null) {
                resetStatistics();
                return;
            }

            vertexCountLabel.setText(String.valueOf(model.getVertices().size()));
            polygonCountLabel.setText(String.valueOf(model.getPolygons().size()));
            textureCountLabel.setText(String.valueOf(model.getTextureVertices().size()));
            normalCountLabel.setText(String.valueOf(model.getNormals().size()));
        });
    }

    private void resetStatistics() {
        Platform.runLater(() -> {
            vertexCountLabel.setText("0");
            polygonCountLabel.setText("0");
            textureCountLabel.setText("0");
            normalCountLabel.setText("0");
        });
    }

    @Override
    public void updateUIFromSelectedObject() {
        Platform.runLater(() -> {
            if (hasSelectedObject()) {
                SceneObject selectedObject = getSelectedObject();
                if (selectedObject != null) {
                    populateFields(selectedObject);
                }
                setFieldsEditable(true);
            } else {
                clearFields();
                setFieldsEditable(false);
            }
        });
    }

    @Override
    protected void setFieldsEditable(boolean editable) {
        Platform.runLater(() -> {
            UiFieldUtils.setTextFieldsEditable(editable, vertexIndicesField, polygonIndicesField);

            vertexIndicesField.setDisable(!editable);
            polygonIndicesField.setDisable(!editable);
            cleanUnusedCheckbox.setDisable(!editable);
            removeVerticesButton.setDisable(!editable);
            removePolygonsButton.setDisable(!editable);
            selectVerticesButton.setDisable(!editable);
            selectPolygonsButton.setDisable(!editable);
            deselectVerticesButton.setDisable(!editable);
            deselectPolygonsButton.setDisable(!editable);
        });
    }
}
