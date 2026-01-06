package ru.vsu.cs.cg.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exceptions.ModelLoadException;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.json.JavaFxJacksonModule;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.service.SceneService;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;
import ru.vsu.cs.cg.utils.validation.InputValidator;
import ru.vsu.cs.cg.utils.constants.MessageConstants;
import ru.vsu.cs.cg.utils.file.PathManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SceneServiceImpl implements SceneService {
    private static final Logger LOG = LoggerFactory.getLogger(SceneServiceImpl.class);

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    private static final String SCENE_EXTENSION = ".3dscene";

    private final ModelService modelService;

    public SceneServiceImpl(ModelService modelService) {
        this.modelService = modelService;
        LOG.debug("SceneServiceImpl инициализирован");
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mapper.registerModule(new JavaFxJacksonModule());
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    @Override
    public Scene createNewScene() {
        Scene scene = new Scene();
        scene.setName("Сцена_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        LOG.info("Создана новая сцена: {}", scene.getName());
        return scene;
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
            LOG.error("Ошибка загрузки сцены из файла '{}': {}", filePath, e.getMessage(), e);
            throw new ModelLoadException("Ошибка загрузки сцены: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка при загрузке сцены '{}': {}", filePath, e.getMessage(), e);
            throw new ModelLoadException(MessageConstants.MODEL_LOAD_ERROR, e);
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

        } catch (Exception e) {
            LOG.error("Ошибка сохранения сцены в файл {}: {}", filePath, e.getMessage(), e);
            throw e;
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

        } catch (Exception e) {
            LOG.error("Ошибка добавления модели '{}' в сцену: {}", modelFilePath, e.getMessage(), e);
            throw e;
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

        } catch (Exception e) {
            LOG.error("Ошибка добавления стандартной модели '{}' в сцену: {}", modelType, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void removeSelectedObject(Scene scene) {
        if (scene.getSelectedObject() == null) {
            LOG.warn("Попытка удалить объект при отсутствии выбора");
            return;
        }

        SceneObject selected = scene.getSelectedObject();
        scene.removeObject(selected);
        LOG.info("Выбранный объект '{}' удален из сцены", selected.getName());
    }

    @Override
    public void duplicateSelectedObject(Scene scene) {
        if (scene.getSelectedObject() == null) {
            LOG.warn("Попытка дублировать объект при отсутствии выбора");
            return;
        }

        SceneObject selected = scene.getSelectedObject();
        SceneObject duplicate = selected.copy();
        scene.addObject(duplicate);
        scene.selectObject(duplicate);

        LOG.info("Объект '{}' продублирован как '{}'", selected.getName(), duplicate.getName());
    }

    private SceneObject createUniqueSceneObject(Scene scene, String baseName, Model model) {
        InputValidator.validateNotEmpty(baseName, "Базовое имя объекта");
        InputValidator.validateNotNull(model, "Модель");

        String uniqueName = baseName;
        int counter = 1;

        while (scene.findObjectByName(uniqueName).isPresent()) {
            uniqueName = baseName + "." + String.format("%03d", counter++);
        }

        return new SceneObject(uniqueName, model);
    }

    private void validateScene(Scene scene) {
        InputValidator.validateNotNull(scene, "Сцена");
        InputValidator.validateNotEmpty(scene.getName(), "Имя сцены");

        LOG.debug("Сцена валидирована: name='{}', objects={}", scene.getName(), scene.getObjectCount());
    }
}
