package ru.vsu.cs.cg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.vsu.cs.cg.exception.ApplicationException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;
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

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public class SceneController {

    private static final Logger LOG = LoggerFactory.getLogger(SceneController.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();

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
        try {
            this.modelService = new ModelServiceImpl();
            this.sceneService = new SceneServiceImpl(modelService);
            this.currentScene = sceneService.createNewScene();
            this.clipboardObject = null;
            this.sceneModified = false;
            this.currentSceneFilePath = null;
            this.uiUpdateInProgress = false;

            LOG.info("SceneController создан с новой сценой: {}", currentScene.getName());
        } catch (Exception e) {
            LOG.error("Ошибка создания SceneController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, APPLICATION_INIT_ERROR);
            throw new ApplicationException(APPLICATION_INIT_ERROR + ": SceneController", e);
        }
    }

    public void setTransformController(TransformController transformController) {
        try {
            this.transformController = transformController;
            LOG.debug("TransformController установлен в SceneController");
        } catch (Exception e) {
            LOG.error("Ошибка установки TransformController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    public void setMaterialController(MaterialController materialController) {
        try {
            this.materialController = materialController;
            LOG.debug("MaterialController установлен в SceneController");
        } catch (Exception e) {
            LOG.error("Ошибка установки MaterialController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    public void setMainController(MainController mainController) {
        try {
            this.mainController = mainController;
            LOG.debug("MainController установлен в SceneController");
        } catch (Exception e) {
            LOG.error("Ошибка установки MainController: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
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
            LOG.error("Ошибка добавления модели из файла {}: {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + filePath, e);
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
            LOG.error("Ошибка добавления стандартной модели '{}': {}", modelType.getDisplayName(), e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + modelType.getDisplayName(), e);
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
            LOG.error("Ошибка создания пользовательского объекта: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": создание пользовательского объекта", e);
        }
    }

    public void removeSelectedObject() {
        try {
            if (!hasSelectedObject()) {
                LOG.warn("Попытка удалить объект при отсутствии выбора");
                return;
            }

            String objectName = currentScene.getSelectedObject().getName();
            sceneService.removeSelectedObject(currentScene);
            markSceneModified();
            updateUI();
            LOG.info("Объект '{}' удален из сцены", objectName);
        } catch (Exception e) {
            LOG.error("Ошибка удаления объекта: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, UI_OPERATION_ERROR);
            throw new ApplicationException(UI_OPERATION_ERROR + ": удаление объекта", e);
        }
    }

    public void duplicateSelectedObject() {
        try {
            if (!hasSelectedObject()) {
                LOG.warn("Попытка дублировать объект при отсутствии выбора");
                return;
            }

            sceneService.duplicateSelectedObject(currentScene);
            markSceneModified();
            updateUI();
            LOG.info("Выбранный объект продублирован");
        } catch (Exception e) {
            LOG.error("Ошибка дублирования объекта: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, UI_OPERATION_ERROR);
            throw new ApplicationException(UI_OPERATION_ERROR + ": дублирование объекта", e);
        }
    }

    public void copySelectedObject() {
        try {
            if (!hasSelectedObject()) {
                LOG.warn("Попытка копировать объект при отсутствии выбора");
                return;
            }

            clipboardObject = currentScene.getSelectedObject().copy();
            LOG.info("Объект '{}' скопирован в буфер обмена", clipboardObject.getName());
        } catch (Exception e) {
            LOG.error("Ошибка копирования объекта: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, UI_OPERATION_ERROR);
            throw new ApplicationException(UI_OPERATION_ERROR + ": копирование объекта", e);
        }
    }

    public void pasteCopiedObject() {
        try {
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
        } catch (Exception e) {
            LOG.error("Ошибка вставки объекта: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, UI_OPERATION_ERROR);
            throw new ApplicationException(UI_OPERATION_ERROR + ": вставка объекта", e);
        }
    }

    public void saveSelectedModelToFile(String filePath) {
        try {
            if (!hasSelectedObject()) {
                throw new IllegalStateException("Нет выбранного объекта для сохранения");
            }

            Model modelToSave = currentScene.getSelectedObject().getModel();
            if (modelToSave == null) {
                throw new IllegalStateException("У выбранного объекта нет модели");
            }

            modelService.saveModelToFile(modelToSave, filePath);
            LOG.info("Модель сохранена в файл: {}", filePath);
        } catch (Exception e) {
            LOG.error("Ошибка сохранения модели в файл {}: {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, OBJECT_SAVE_ERROR);
            throw new ApplicationException(OBJECT_SAVE_ERROR + ": " + filePath, e);
        }
    }

    public void createNewScene() {
        try {
            currentScene = sceneService.createNewScene();
            clipboardObject = null;
            sceneModified = false;
            currentSceneFilePath = null;
            updateUI();
            LOG.info("Создана новая сцена: {}", currentScene.getName());
        } catch (Exception e) {
            LOG.error("Ошибка создания новой сцены: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw new ApplicationException(SCENE_LOAD_ERROR + ": создание новой сцены", e);
        }
    }

    public void saveScene(String filePath) {
        try {
            sceneService.saveScene(currentScene, filePath);
            sceneModified = false;
            currentSceneFilePath = filePath;
            LOG.info("Сцена сохранена в файл: {}", filePath);
        } catch (IOException e) {
            LOG.error("Ошибка сохранения сцены в файл {}: {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_SAVE_ERROR);
            throw new ApplicationException(SCENE_SAVE_ERROR + ": " + filePath, e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка сохранения сцены в файл {}: {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_SAVE_ERROR);
            throw new ApplicationException(SCENE_SAVE_ERROR, e);
        }
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
            LOG.error("Ошибка загрузки сцены из файла {}: {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw new ApplicationException(SCENE_LOAD_ERROR + ": " + filePath, e);
        }
    }

    public boolean hasUnsavedChanges() {
        try {
            return sceneModified;
        } catch (Exception e) {
            LOG.error("Ошибка проверки изменений сцены: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return false;
        }
    }

    public void markSceneModified() {
        try {
            if (!sceneModified) {
                sceneModified = true;
                LOG.debug("Сцена отмечена как измененная");
            }
        } catch (Exception e) {
            LOG.error("Ошибка отметки сцены как измененной: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    public void handleSceneObjectSelection(String objectName) {
        try {
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
        } catch (Exception e) {
            LOG.error("Ошибка выбора объекта сцены '{}': {}", objectName, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
        }
    }

    public void resetTransformOfSelectedObject() {
        try {
            if (!hasSelectedObject()) {
                LOG.warn("Попытка сбросить трансформацию при отсутствии выбранного объекта");
                return;
            }

            currentScene.getSelectedObject().getTransform().reset();
            markSceneModified();
            updateUI();
            LOG.info("Трансформация объекта '{}' сброшена", currentScene.getSelectedObject().getName());
        } catch (Exception e) {
            LOG.error("Ошибка сброса трансформации объекта: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TRANSFORMATION_ERROR);
            throw new ApplicationException(TRANSFORMATION_ERROR + ": сброс трансформации", e);
        }
    }

    public void applyTransformToSelectedObject(double posX, double posY, double posZ,
                                               double rotX, double rotY, double rotZ,
                                               double scaleX, double scaleY, double scaleZ) {
        try {
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
        } catch (Exception e) {
            LOG.error("Ошибка применения трансформации к объекту: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, TRANSFORMATION_ERROR);
            throw new ApplicationException(TRANSFORMATION_ERROR + ": применение трансформации", e);
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

    private String generateUniqueCopyName(String baseName) {
        try {
            String copyName = baseName + "_copy";
            int counter = 1;

            while (currentScene.findObjectByName(copyName).isPresent()) {
                copyName = baseName + "_copy" + counter++;
            }

            return copyName;
        } catch (Exception e) {
            LOG.error("Ошибка генерации уникального имени копии '{}': {}", baseName, e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return baseName + "_copy";
        }
    }

    private void updateUI() {
        try {
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
                LOG.error("Ошибка обновления UI контроллеров: {}", e.getMessage(), e);
                EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, UI_UPDATE_ERROR);
            } finally {
                uiUpdateInProgress = false;
            }
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка обновления UI: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            uiUpdateInProgress = false;
        }
    }

    private void updateUIWithoutTreeSelection() {
        try {
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
                LOG.error("Ошибка обновления UI контроллеров (без дерева): {}", e.getMessage(), e);
                EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, UI_UPDATE_ERROR);
            } finally {
                uiUpdateInProgress = false;
            }
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка обновления UI (без дерева): {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            uiUpdateInProgress = false;
        }
    }
}
