package ru.vsu.cs.cg.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exception.ApplicationException;
import ru.vsu.cs.cg.exception.FileOperationException;
import ru.vsu.cs.cg.exception.ModelLoadException;
import ru.vsu.cs.cg.exception.ValidationException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.json.JavaFxJacksonModule;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.service.SceneService;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;
import ru.vsu.cs.cg.utils.validation.InputValidator;
import ru.vsu.cs.cg.utils.file.PathManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public class SceneServiceImpl implements SceneService {
    private static final Logger LOG = LoggerFactory.getLogger(SceneServiceImpl.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    private static final String SCENE_EXTENSION = ".3dscene";

    private final ModelService modelService;

    public SceneServiceImpl(ModelService modelService) {
        try {
            this.modelService = modelService;
            LOG.debug("SceneServiceImpl инициализирован");
        } catch (Exception e) {
            LOG.error("Ошибка инициализации SceneServiceImpl: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, APPLICATION_INIT_ERROR);
            throw new ApplicationException(APPLICATION_INIT_ERROR + ": SceneServiceImpl", e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        try {
            ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            mapper.registerModule(new JavaFxJacksonModule());
            mapper.registerModule(new JavaTimeModule());

            return mapper;
        } catch (Exception e) {
            LOG.error("Ошибка создания ObjectMapper: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return new ObjectMapper();
        }
    }

    @Override
    public Scene createNewScene() {
        try {
            Scene scene = new Scene();
            scene.setName("Сцена_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
            LOG.info("Создана новая сцена: {}", scene.getName());
            return scene;
        } catch (Exception e) {
            LOG.error("Ошибка создания новой сцены: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw new ApplicationException(SCENE_LOAD_ERROR + ": создание новой сцены", e);
        }
    }

    @Override
    public Scene loadScene(String filePath) {
        LOG.info("Загрузка сцены из файла: {}", filePath);

        try {
            InputValidator.validateNotEmpty(filePath, "Путь к файлу сцены");
            PathManager.validatePathForRead(filePath);

            if (!PathManager.isSupportedSceneFormat(filePath)) {
                LOG.warn("Файл сцены имеет нестандартное расширение: {}", filePath);
            }

            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            Scene scene = OBJECT_MAPPER.readValue(content, Scene.class);

            LOG.info("Сцена '{}' успешно загружена. Объектов: {}",
                scene.getName(), scene.getObjectCount());
            return scene;

        } catch (IOException e) {
            String errorMessage = SCENE_LOAD_ERROR + ": " + filePath + " - " + e.getMessage();
            LOG.error(errorMessage, e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw new ModelLoadException(errorMessage, e);
        } catch (ModelLoadException e) {
            LOG.error("Ошибка загрузки модели сцены из файла '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw e;
        } catch (ValidationException e) {
            LOG.error("Ошибка валидации пути сцены '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw new ApplicationException(SCENE_LOAD_ERROR + ": " + filePath, e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка при загрузке сцены '{}': {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw new ModelLoadException(SCENE_LOAD_ERROR, e);
        }
    }

    @Override
    public void saveScene(Scene scene, String filePath) throws IOException {
        LOG.info("Сохранение сцены в файл: {}", filePath);

        try {
            validateScene(scene);
            InputValidator.validateNotEmpty(filePath, "Путь к файлу");

            String normalizedPath = PathManager.normalizePath(filePath);
            normalizedPath = PathManager.ensureExtension(normalizedPath, SCENE_EXTENSION);
            PathManager.validatePathForSave(normalizedPath);

            String json = OBJECT_MAPPER.writeValueAsString(scene);
            Files.write(Paths.get(normalizedPath), json.getBytes());

            LOG.info("Сцена '{}' успешно сохранена в файл: {}. Объектов: {}",
                scene.getName(), normalizedPath, scene.getObjectCount());

        } catch (IOException e) {
            String errorMessage = SCENE_SAVE_ERROR + ": " + filePath + " - " + e.getMessage();
            LOG.error(errorMessage, e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_SAVE_ERROR);
            throw new FileOperationException(errorMessage, e);
        } catch (ValidationException e) {
            LOG.error("Ошибка валидации при сохранении сцены в файл {}: {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_SAVE_ERROR);
            throw new ApplicationException(SCENE_SAVE_ERROR + ": " + filePath, e);
        } catch (FileOperationException e) {
            LOG.error("Ошибка операции с файлом при сохранении сцены {}: {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_SAVE_ERROR);
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка сохранения сцены в файл {}: {}", filePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_SAVE_ERROR);
            throw new ApplicationException(SCENE_SAVE_ERROR + ": " + filePath, e);
        }
    }

    @Override
    public SceneObject addModelToScene(Scene scene, String modelFilePath) {
        LOG.info("Добавление модели в сцену: {}", modelFilePath);

        try {
            InputValidator.validateNotEmpty(modelFilePath, "Путь к файлу модели");
            PathManager.validatePathForRead(modelFilePath);

            if (!PathManager.isSupported3DFormat(modelFilePath)) {
                LOG.warn("Файл модели имеет нестандартное расширение: {}", modelFilePath);
            }

            Model model = modelService.loadModel(modelFilePath);
            String modelName = PathManager.getFileNameWithoutExtension(modelFilePath);

            SceneObject sceneObject = createUniqueSceneObject(scene, modelName, model);
            scene.addObject(sceneObject);

            LOG.info("Модель '{}' добавлена в сцену как объект '{}'", modelFilePath, sceneObject.getName());
            return sceneObject;

        } catch (ModelLoadException e) {
            LOG.error("Ошибка загрузки модели '{}' для добавления в сцену: {}", modelFilePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + modelFilePath, e);
        } catch (ValidationException e) {
            LOG.error("Ошибка валидации при добавлении модели '{}' в сцену: {}", modelFilePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + modelFilePath, e);
        } catch (FileOperationException e) {
            LOG.error("Ошибка операции с файлом при добавлении модели '{}' в сцену: {}", modelFilePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + modelFilePath, e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка добавления модели '{}' в сцену: {}", modelFilePath, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + modelFilePath, e);
        }
    }

    @Override
    public SceneObject addDefaultModelToScene(Scene scene, String modelType) {
        LOG.info("Добавление стандартной модели в сцену: {}", modelType);

        try {
            InputValidator.validateNotEmpty(modelType, "Тип модели");
            DefaultModelLoader.ModelType type = DefaultModelLoader.ModelType.valueOf(modelType.toUpperCase());
            Model model = modelService.loadDefaultModel(type);

            SceneObject sceneObject = createUniqueSceneObject(scene, type.getDisplayName(), model);
            scene.addObject(sceneObject);

            LOG.info("Стандартная модель '{}' добавлена в сцену как объект '{}'",
                type.getDisplayName(), sceneObject.getName());
            return sceneObject;

        } catch (IllegalArgumentException e) {
            String errorMessage = "Неизвестный тип модели: " + modelType;
            LOG.error(errorMessage, e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + errorMessage, e);
        } catch (ModelLoadException e) {
            LOG.error("Ошибка загрузки стандартной модели '{}': {}", modelType, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + modelType, e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка добавления стандартной модели '{}' в сцену: {}", modelType, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": " + modelType, e);
        }
    }

    @Override
    public void removeSelectedObject(Scene scene) {
        try {
            if (scene.getSelectedObject() == null) {
                LOG.warn("Попытка удалить объект при отсутствии выбора");
                return;
            }

            SceneObject selected = scene.getSelectedObject();
            scene.removeObject(selected);
            LOG.info("Выбранный объект '{}' удален из сцены", selected.getName());
        } catch (Exception e) {
            LOG.error("Ошибка удаления выбранного объекта из сцены: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw new ApplicationException(SCENE_LOAD_ERROR + ": удаление объекта", e);
        }
    }

    @Override
    public void duplicateSelectedObject(Scene scene) {
        try {
            if (scene.getSelectedObject() == null) {
                LOG.warn("Попытка дублировать объект при отсутствии выбора");
                return;
            }

            SceneObject selected = scene.getSelectedObject();
            SceneObject duplicate = selected.copy();
            scene.addObject(duplicate);
            scene.selectObject(duplicate);

            LOG.info("Объект '{}' продублирован как '{}'", selected.getName(), duplicate.getName());
        } catch (Exception e) {
            LOG.error("Ошибка дублирования выбранного объекта в сцене: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_LOAD_ERROR);
            throw new ApplicationException(SCENE_LOAD_ERROR + ": дублирование объекта", e);
        }
    }

    private SceneObject createUniqueSceneObject(Scene scene, String baseName, Model model) {
        try {
            InputValidator.validateNotEmpty(baseName, "Базовое имя объекта");
            InputValidator.validateNotNull(model, "Модель");

            String uniqueName = baseName;
            int counter = 1;

            while (scene.findObjectByName(uniqueName).isPresent()) {
                uniqueName = baseName + "." + String.format("%03d", counter++);
            }

            return new SceneObject(uniqueName, model);
        } catch (ValidationException e) {
            LOG.error("Ошибка валидации при создании объекта сцены '{}': {}", baseName, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": создание объекта сцены", e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка создания объекта сцены '{}': {}", baseName, e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ApplicationException(MODEL_LOAD_ERROR + ": создание объекта сцены", e);
        }
    }

    private void validateScene(Scene scene) {
        try {
            InputValidator.validateNotNull(scene, "Сцена");
            InputValidator.validateNotEmpty(scene.getName(), "Имя сцены");

            LOG.debug("Сцена валидирована: name='{}', objects={}", scene.getName(), scene.getObjectCount());
        } catch (ValidationException e) {
            LOG.error("Ошибка валидации сцены: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_VALIDATION_ERROR);
            throw new ApplicationException(SCENE_VALIDATION_ERROR, e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка валидации сцены: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, SCENE_VALIDATION_ERROR);
            throw new ApplicationException(SCENE_VALIDATION_ERROR, e);
        }
    }
}
