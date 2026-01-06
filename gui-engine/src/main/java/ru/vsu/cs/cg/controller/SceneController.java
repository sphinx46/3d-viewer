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
    private MainController mainController;
    private Scene currentScene;
    private SceneObject clipboardObject;
    private boolean sceneModified = false;
    private String currentSceneFilePath = null;
    private boolean uiUpdateInProgress = false;

    public SceneController() {
        this.modelService = new ModelServiceImpl();
        this.sceneService = new SceneServiceImpl(modelService);
        this.currentScene = sceneService.createNewScene();
        this.clipboardObject = null;
        this.sceneModified = false;
        this.currentSceneFilePath = null;
        this.uiUpdateInProgress = false;

        LOG.info("SceneController создан с новой сценой: {}", currentScene.getName());
    }

    public void setTransformController(TransformController transformController) {
        this.transformController = transformController;
        LOG.debug("TransformController установлен в SceneController");
    }

    public void setMaterialController(MaterialController materialController) {
        this.materialController = materialController;
        LOG.debug("MaterialController установлен в SceneController");
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        LOG.debug("MainController установлен в SceneController");
    }

    public SceneObject addModelToScene(String filePath) {
        try {
            SceneObject newObject = sceneService.addModelToScene(currentScene, filePath);
            currentScene.selectObject(newObject);
            markSceneModified();
            updateUI();
            LOG.info("Модель '{}' добавлена в сцену из файла: {}", newObject.getName(), filePath);
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
            updateUI();
            LOG.info("Стандартная модель '{}' добавлена в сцену", modelType.getDisplayName());
            return newObject;
        } catch (Exception e) {
            LOG.error("Ошибка добавления стандартной модели '{}': {}", modelType.getDisplayName(), e.getMessage());
            throw e;
        }
    }

    public SceneObject createCustomObject() {
        try {
            SceneObject newObject = sceneService.addDefaultModelToScene(currentScene, "CUBE");
            currentScene.selectObject(newObject);
            markSceneModified();
            updateUI();
            LOG.info("Пользовательский объект создан: {}", newObject.getName());
            return newObject;
        } catch (Exception e) {
            LOG.error("Ошибка создания пользовательского объекта: {}", e.getMessage());
            throw e;
        }
    }

    public void removeSelectedObject() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка удалить объект при отсутствии выбора");
            return;
        }

        String objectName = currentScene.getSelectedObject().getName();
        sceneService.removeSelectedObject(currentScene);
        markSceneModified();
        updateUI();
        LOG.info("Объект '{}' удален из сцены", objectName);
    }

    public void duplicateSelectedObject() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка дублировать объект при отсутствии выбора");
            return;
        }

        sceneService.duplicateSelectedObject(currentScene);
        markSceneModified();
        updateUI();
        LOG.info("Выбранный объект продублирован");
    }

    public void copySelectedObject() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка копировать объект при отсутствии выбора");
            return;
        }

        clipboardObject = currentScene.getSelectedObject().copy();
        LOG.info("Объект '{}' скопирован в буфер обмена", clipboardObject.getName());
    }

    public void pasteCopiedObject() {
        if (clipboardObject == null) {
            LOG.warn("Попытка вставить объект из пустого буфера");
            return;
        }

        SceneObject pastedObject = clipboardObject.copy();
        pastedObject.setName(generateUniqueCopyName(clipboardObject.getName()));
        currentScene.addObject(pastedObject);
        currentScene.selectObject(pastedObject);
        markSceneModified();
        updateUI();
        LOG.info("Объект '{}' вставлен из буфера обмена", pastedObject.getName());
    }

    public void saveSelectedModelToFile(String filePath) {
        if (!hasSelectedObject()) {
            throw new IllegalStateException("Нет выбранного объекта для сохранения");
        }

        Model modelToSave = currentScene.getSelectedObject().getModel();
        if (modelToSave == null) {
            throw new IllegalStateException("У выбранного объекта нет модели");
        }

        modelService.saveModelToFile(modelToSave, filePath);
        LOG.info("Модель сохранена в файл: {}", filePath);
    }

    public void createNewScene() {
        currentScene = sceneService.createNewScene();
        clipboardObject = null;
        sceneModified = false;
        currentSceneFilePath = null;
        updateUI();
        LOG.info("Создана новая сцена: {}", currentScene.getName());
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
            currentSceneFilePath = filePath;
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

    public void markSceneModified() {
        if (!sceneModified) {
            sceneModified = true;
            LOG.debug("Сцена отмечена как измененная");
        }
    }

    public void handleSceneObjectSelection(String objectName) {
        if (uiUpdateInProgress) {
            return;
        }

        Optional<SceneObject> foundObject = currentScene.findObjectByName(objectName);
        if (foundObject.isPresent()) {
            currentScene.selectObject(foundObject.get());
            updateUIWithoutTreeSelection();
            LOG.debug("Выбран объект сцены: {}", objectName);
        } else {
            LOG.warn("Объект с именем '{}' не найден в сцене", objectName);
        }
    }

    public void resetTransformOfSelectedObject() {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка сбросить трансформацию при отсутствии выбранного объекта");
            return;
        }

        currentScene.getSelectedObject().getTransform().reset();
        markSceneModified();
        updateUI();
        LOG.info("Трансформация объекта '{}' сброшена", currentScene.getSelectedObject().getName());
    }

    public void applyTransformToSelectedObject(double posX, double posY, double posZ,
                                               double rotX, double rotY, double rotZ,
                                               double scaleX, double scaleY, double scaleZ) {
        if (!hasSelectedObject()) {
            LOG.warn("Попытка применить трансформацию при отсутствии выбранного объекта");
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
        LOG.info("Трансформация применена к объекту '{}'", currentScene.getSelectedObject().getName());
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

    private void updateUI() {
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
            if (mainController != null) {
                mainController.updateSceneTree();
            }
        } catch (Exception e) {
            LOG.error("Ошибка обновления UI контроллеров: {}", e.getMessage());
        } finally {
            uiUpdateInProgress = false;
        }
    }

    private void updateUIWithoutTreeSelection() {
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
        } catch (Exception e) {
            LOG.error("Ошибка обновления UI контроллеров: {}", e.getMessage());
        } finally {
            uiUpdateInProgress = false;
        }
    }
}
