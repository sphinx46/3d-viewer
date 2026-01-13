package ru.vsu.cs.cg.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.model.Model;

public class ModificationController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(ModificationController.class);

    @FXML private TextField vertexIndicesField;
    @FXML private CheckBox cleanUnusedCheckbox;
    @FXML private Button removeVerticesButton;
    @FXML private Button selectVerticesButton;

    @FXML private TextField polygonIndicesField;
    @FXML private Button removePolygonsButton;
    @FXML private Button selectPolygonsButton;

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
    }

    private void handleRemoveVertices() {
        if (!hasSelectedObject()) {
            return;
        }

        String indicesInput = vertexIndicesField.getText();
        boolean clearUnused = cleanUnusedCheckbox.isSelected();

        ru.vsu.cs.cg.controller.command.Command command =
            new ru.vsu.cs.cg.controller.command.impl.modification.RemoveVerticesCommand(
                sceneController, indicesInput, clearUnused);
        command.execute();

        updateStatistics();
    }

    private void handleRemovePolygons() {
        if (!hasSelectedObject()) {
            return;
        }

        String indicesInput = polygonIndicesField.getText();

        ru.vsu.cs.cg.controller.command.Command command =
            new ru.vsu.cs.cg.controller.command.impl.modification.RemovePolygonsCommand(
                sceneController, indicesInput);
        command.execute();

        updateStatistics();
    }

    private void handleSelectVertices() {
        LOG.debug("Выделение вершин по индексам");
    }

    private void handleSelectPolygons() {
        LOG.debug("Выделение полигонов по индексам");
    }

    @Override
    protected void clearFields() {
        Platform.runLater(() -> {
            vertexIndicesField.clear();
            polygonIndicesField.clear();
            cleanUnusedCheckbox.setSelected(false);
            resetStatistics();
        });
    }

    @Override
    protected void populateFields(SceneObject object) {
        updateStatistics();
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
            vertexIndicesField.setEditable(editable);
            vertexIndicesField.setDisable(!editable);
            polygonIndicesField.setEditable(editable);
            polygonIndicesField.setDisable(!editable);
            cleanUnusedCheckbox.setDisable(!editable);
            removeVerticesButton.setDisable(!editable);
            removePolygonsButton.setDisable(!editable);
            selectVerticesButton.setDisable(!editable);
            selectPolygonsButton.setDisable(!editable);
        });
    }
}
