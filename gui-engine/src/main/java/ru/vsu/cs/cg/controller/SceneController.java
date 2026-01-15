package ru.vsu.cs.cg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.enums.TransformationMode;
import ru.vsu.cs.cg.controller.handlers.MouseTransformationHandler;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.rasterization.RasterizerSettings;
import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.scene.Transform;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.service.SceneService;
import ru.vsu.cs.cg.service.impl.ModelServiceImpl;
import ru.vsu.cs.cg.service.impl.SceneServiceImpl;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;
import ru.vsu.cs.cg.utils.scene.SceneUtils;
import ru.vsu.cs.cg.renderEngine.camera.Camera;
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
    private CameraController cameraController;
    private MouseTransformationHandler mouseTransformationHandler;
    private Scene currentScene;
    private SceneObject clipboardObject;
    private boolean sceneModified = false;
    private boolean modelModified = false;
    private String currentSceneFilePath = null;
    private boolean uiUpdateInProgress = false;
    private TransformationMode currentTransformationMode = TransformationMode.NONE;

    public SceneController() {
        this.modelService = new ModelServiceImpl();
        this.sceneService = new SceneServiceImpl(modelService);
        this.currentScene = sceneService.createNewScene();
        this.mouseTransformationHandler = new MouseTransformationHandler(this);
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
    
    public void setTransformationMode(TransformationMode mode) {
        if (this.currentTransformationMode == mode && mode != TransformationMode.NONE) {
            mode = TransformationMode.NONE;
        }

        this.currentTransformationMode = mode;
        if (mouseTransformationHandler != null) {
            mouseTransformationHandler.setTransformationMode(mode);
        }
        LOG.info("Установлен режим трансформации: {}", mode);

        if (mainController != null) {
            mainController.updateTransformationButtons(mode);
        }
    }


    public TransformationMode getTransformationMode() {
        return currentTransformationMode;
    }

    public MouseTransformationHandler getMouseTransformationHandler() {
        return mouseTransformationHandler;
    }

    public SceneObject addModelToScene(String filePath) {
        try {
            SceneObject newObject = sceneService.addModelToScene(currentScene, filePath);
            newObject.getRenderSettings().setDrawAxisLines(true);
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
            newObject.getRenderSettings().setDrawAxisLines(true);
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
        LOG.debug("Объект '{}' скопирован в буфер обмена", clipboardObject.getName());
    }

    public void pasteCopiedObject() {
        if (clipboardObject == null) {
            return;
        }

        SceneObject pastedObject = clipboardObject.copy();
        pastedObject.setName(SceneUtils.generateUniqueCopyName(clipboardObject.getName(), currentScene));
        pastedObject.getRenderSettings().setDrawAxisLines(true);
        currentScene.addObject(pastedObject);
        currentScene.selectObject(pastedObject);
        markSceneModified();
        markModelModified();
        updateUI();
        LOG.debug("Объект '{}' вставлен из буфера обмена", pastedObject.getName());
    }

    public void renameSelectedObject(String newName) {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка переименования без выбранного объекта");
            return;
        }

        SceneUtils.validateAndRenameObject(getSelectedObject(), newName, currentScene);
        markSceneModified();
        updateUI();
    }

    public void createNewScene() {
        currentScene = sceneService.createNewScene();
        clipboardObject = null;
        sceneModified = false;
        modelModified = false;
        currentSceneFilePath = null;
        currentTransformationMode = TransformationMode.NONE;

        if (mouseTransformationHandler != null) {
            mouseTransformationHandler.setTransformationMode(TransformationMode.NONE);
        }

        if (renderController != null) {
            renderController.setScene(currentScene);
        }

        updateUI();
        LOG.info("Создана новая сцена");
    }

    public void saveScene(String filePath) throws IOException {
        sceneService.saveScene(currentScene, filePath);
        sceneModified = false;
        currentSceneFilePath = filePath;
        LOG.info("Сцена сохранена в файл: {}", filePath);
    }

    public void loadScene(String filePath) {
        try {
            currentScene = sceneService.loadScene(filePath);
            clipboardObject = null;
            sceneModified = false;
            modelModified = false;
            currentSceneFilePath = filePath;
            currentTransformationMode = TransformationMode.NONE;

            if (mouseTransformationHandler != null) {
                mouseTransformationHandler.setTransformationMode(TransformationMode.NONE);
            }

            if (renderController != null) {
                renderController.setScene(currentScene);
            }

            updateUI();
            LOG.info("Сцена загружена из файла: {}", filePath);
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
                LOG.debug("Выбран объект: {}", objectName);
            }
        }
    }

    public void toggleObjectVisibility(SceneObject object) {
        if (object == null) return;

        boolean newVisibility = !object.isVisible();
        object.setVisible(newVisibility);
        markSceneModified();
        updateUI();
        LOG.info("Видимость объекта '{}' переключена: {}", object.getName(), newVisibility);
    }

    public void resetTransformOfSelectedObject() {
        if (!hasSelectedObject()) {
            return;
        }

        currentScene.getSelectedObject().getTransform().reset();
        markSceneModified();
        markModelModified();
        updateUI();
        LOG.debug("Трансформация объекта '{}' сброшена", getSelectedObject().getName());
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
        LOG.debug("Применена трансформация к объекту '{}'", getSelectedObject().getName());
    }

    public void toggleGridVisibility() {
        currentScene.setGridVisible(!currentScene.isGridVisible());
        LOG.info("Видимость сетки переключена: {}", currentScene.isGridVisible());
        markSceneModified();
    }

    public void toggleAxisVisibility() {
        if (hasSelectedObject()) {
            SceneObject selected = getSelectedObject();
            RasterizerSettings settings = selected.getRenderSettings();
            boolean newState = !settings.isDrawAxisLines();
            settings.setDrawAxisLines(newState);
            LOG.info("Оси XYZ для объекта '{}' переключены: {}", selected.getName(), newState);
            markSceneModified();
        }
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

    public void saveSelectedModelWithMaterial(String filePath) {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка сохранения модели без выбранного объекта");
            return;
        }

        SceneObject selectedObject = getSelectedObject();
        Model transformedModel = selectedObject.getTransformedModel();

        ModelServiceImpl modelServiceImpl = (ModelServiceImpl) modelService;
        ru.vsu.cs.cg.scene.Material material = selectedObject.getMaterial();
        RasterizerSettings renderSettings = selectedObject.getRenderSettings();

        String materialName = selectedObject.getName() + "_material";
        String texturePath = material.getTexturePath();

        float[] color = new float[]{
                (float) material.getRed(),
                (float) material.getGreen(),
                (float) material.getBlue()
        };

        Float shininess = renderSettings.isUseLighting() ?
                (float) material.getLightIntensity() : null;

        Float transparency = (float) material.getAmbient();
        Float reflectivity = (float) material.getDiffusion();

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

        LOG.info("Модель '{}' сохранена с материалом в файл: {}", selectedObject.getName(), filePath);
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

    public void addCamera(Vector3f position){
        cameraController.createCamera(position);
    }

    public void setCameraController(CameraController cameraController) {
        this.cameraController = cameraController;
    }

    public Scene getScene(){
        return currentScene;
    }

    public Camera getActiveCamera() {
        if (renderController != null && renderController.getSceneManager() != null) {
            return renderController.getSceneManager().getActiveCamera();
        }
        return null;
    }
}
