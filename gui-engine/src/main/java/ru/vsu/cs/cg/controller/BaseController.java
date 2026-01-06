package ru.vsu.cs.cg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.scene.SceneObject;

public abstract class BaseController {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseController.class);
    protected SceneController sceneController;

    public void setSceneController(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    protected boolean hasSelectedObject() {
        return sceneController != null && sceneController.hasSelectedObject();
    }

    protected SceneObject getSelectedObject() {
        return sceneController != null ? sceneController.getSelectedObject() : null;
    }

    public void updateUIFromSelectedObject() {
        if (!hasSelectedObject()) {
            clearFields();
            setFieldsEditable(false);
        } else {
            populateFields(getSelectedObject());
            setFieldsEditable(true);
        }
    }

    protected abstract void clearFields();
    protected abstract void populateFields(SceneObject object);
    protected abstract void setFieldsEditable(boolean editable);
}
