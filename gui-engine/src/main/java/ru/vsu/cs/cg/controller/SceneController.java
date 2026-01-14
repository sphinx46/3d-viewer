package ru.vsu.cs.cg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.scene.Transform;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.service.SceneService;
import ru.vsu.cs.cg.service.impl.ModelServiceImpl;
import ru.vsu.cs.cg.service.impl.SceneServiceImpl;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;

import java.io.IOException;
import java.util.Optional;

public class SceneController {

    private static final Logger LOG = LoggerFactory.getLogger(SceneController.class);

    private final SceneService sceneService;
    private final ModelService modelService;
    private TransformController transformController;
    private MaterialController materialController;
    private ModificationController modificationController;
    private MainController mainController;
    private RenderController renderController;
    private Scene currentScene;
    private SceneObject clipboardObject;
    private boolean sceneModified = false;
    private boolean modelModified = false;
    private String currentSceneFilePath = null;
    private boolean uiUpdateInProgress = false;

    public SceneController() {
        this.modelService = new ModelServiceImpl();
        this.sceneService = new SceneServiceImpl(modelService);
        this.currentScene = sceneService.createNewScene();
    }

    public void setTransformController(TransformController transformController) {
        this.transformController = transformController;
    }

    public void setMaterialController(MaterialController materialController) {
        this.materialController = materialController;
    }

    public void setModificationController(ModificationController modificationController) {
        this.modificationController = modificationController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setRenderController(RenderController renderController) {
        this.renderController = renderController;
        if (this.currentScene != null) {
            this.renderController.setScene(this.currentScene);
        }
    }

    public SceneObject addModelToScene(String filePath) {
        try {
            SceneObject newObject = sceneService.addModelToScene(currentScene, filePath);
            currentScene.selectObject(newObject);
            markSceneModified();
            markModelModified();
            updateUI();
            return newObject;
        } catch (Exception e) {
            LOG.error("Ошибка добавления модели из файла {}: {}", filePath, e.getMessage());
            throw e;
        }
    }

    public SceneObject addDefaultModelToScene(DefaultModelLoader.ModelType modelType) {
        try {
            SceneObject newObject = sceneService.addDefaultModelToScene(currentScene, modelType.name());
            currentScene.selectObject(newObject);
            markSceneModified();
            markModelModified();
            updateUI();
            return newObject;
        } catch (Exception e) {
            LOG.error("Ошибка добавления стандартной модели '{}': {}", modelType.getDisplayName(), e.getMessage());
            throw e;
        }
    }

    public void removeSelectedObject() {
        if (!hasSelectedObject()) {
            return;
        }

        sceneService.removeSelectedObject(currentScene);
        markSceneModified();
        updateUI();
    }

    public void duplicateSelectedObject() {
        if (!hasSelectedObject()) {
            return;
        }

        sceneService.duplicateSelectedObject(currentScene);
        markSceneModified();
        updateUI();
    }

    public void copySelectedObject() {
        if (!hasSelectedObject()) {
            return;
        }

        clipboardObject = currentScene.getSelectedObject().copy();
    }

    public void pasteCopiedObject() {
        if (clipboardObject == null) {
            return;
        }

        SceneObject pastedObject = clipboardObject.copy();
        pastedObject.setName(generateUniqueCopyName(clipboardObject.getName()));
        currentScene.addObject(pastedObject);
        currentScene.selectObject(pastedObject);
        markSceneModified();
        markModelModified();
        updateUI();
    }

    public void createNewScene() {
        currentScene = sceneService.createNewScene();
        clipboardObject = null;
        sceneModified = false;
        modelModified = false;
        currentSceneFilePath = null;

        if (renderController != null) {
            renderController.setScene(currentScene);
        }

        updateUI();
    }

    public void saveScene(String filePath) throws IOException {
        sceneService.saveScene(currentScene, filePath);
        sceneModified = false;
        currentSceneFilePath = filePath;
    }

    public void loadScene(String filePath) {
        try {
            currentScene = sceneService.loadScene(filePath);
            clipboardObject = null;
            sceneModified = false;
            modelModified = false;
            currentSceneFilePath = filePath;

            if (renderController != null) {
                renderController.setScene(currentScene);
            }

            updateUI();
        } catch (Exception e) {
            LOG.error("Ошибка загрузки сцены из файла {}: {}", filePath, e.getMessage());
            throw e;
        }
    }

    public boolean hasUnsavedChanges() {
        return sceneModified;
    }

    public boolean isModelModified() {
        return modelModified;
    }

    public void markSceneModified() {
        sceneModified = true;
    }

    public void markModelModified() {
        modelModified = true;
        markSceneModified();
    }

    public void handleSceneObjectSelection(String objectName) {
        if (uiUpdateInProgress) {
            return;
        }

        Optional<SceneObject> foundObject = currentScene.findObjectByName(objectName);
        if (foundObject.isPresent()) {
            SceneObject objectToSelect = foundObject.get();
            if (currentScene.getSelectedObject() != objectToSelect) {
                currentScene.selectObject(objectToSelect);
                updateUI();
            }
        }
    }

    public void resetTransformOfSelectedObject() {
        if (!hasSelectedObject()) {
            return;
        }

        currentScene.getSelectedObject().getTransform().reset();
        markSceneModified();
        markModelModified();
        updateUI();
    }

    public void applyTransformToSelectedObject(double posX, double posY, double posZ,
                                               double rotX, double rotY, double rotZ,
                                               double scaleX, double scaleY, double scaleZ) {
        if (!hasSelectedObject()) {
            return;
        }

        Transform transform = currentScene.getSelectedObject().getTransform();
        transform.setPositionX(posX);
        transform.setPositionY(posY);
        transform.setPositionZ(posZ);
        transform.setRotationX(rotX);
        transform.setRotationY(rotY);
        transform.setRotationZ(rotZ);
        transform.setScaleX(scaleX);
        transform.setScaleY(scaleY);
        transform.setScaleZ(scaleZ);

        markSceneModified();
        markModelModified();
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public SceneObject getSelectedObject() {
        return currentScene.getSelectedObject();
    }

    public boolean hasSelectedObject() {
        return currentScene.getSelectedObject() != null;
    }

    public String getCurrentSceneFilePath() {
        return currentSceneFilePath;
    }

    private String generateUniqueCopyName(String baseName) {
        String copyName = baseName + "_copy";
        int counter = 1;

        while (currentScene.findObjectByName(copyName).isPresent()) {
            copyName = baseName + "_copy" + counter++;
        }

        return copyName;
    }

    public void saveSelectedModelWithTransformations(String filePath) {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка сохранения модели без выбранного объекта");
            return;
        }

        SceneObject selectedObject = getSelectedObject();
        Model transformedModel = selectedObject.getTransformedModel();

        ModelServiceImpl modelServiceImpl = (ModelServiceImpl) modelService;
        ru.vsu.cs.cg.scene.Material material = selectedObject.getMaterial();

        String materialName = selectedObject.getName() + "_material";
        String texturePath = material.getTexturePath();
        float[] color = new float[]{
            (float) material.getRed(),
            (float) material.getGreen(),
            (float) material.getBlue()
        };
        Float shininess = (float) material.getShininess();
        Float transparency = (float) material.getTransparency();
        Float reflectivity = (float) material.getReflectivity();

        modelServiceImpl.saveModelWithMaterial(
            transformedModel,
            filePath,
            materialName,
            texturePath,
            color,
            shininess,
            transparency,
            reflectivity
        );

        LOG.info("Модель '{}' сохранена с примененными трансформациями и материалом в файл: {}",
            selectedObject.getName(), filePath);
    }

    public void updateUI() {
        if (uiUpdateInProgress) {
            return;
        }

        uiUpdateInProgress = true;
        try {
            if (transformController != null) {
                transformController.updateUIFromSelectedObject();
            }
            if (materialController != null) {
                materialController.updateUIFromSelectedObject();
            }
            if (modificationController != null) {
                modificationController.updateUIFromSelectedObject();
            }
            if (mainController != null) {
                mainController.updateSceneTree();
            }
        } finally {
            uiUpdateInProgress = false;
        }
    }
}
