package ru.vsu.cs.cg.utils.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exception.ModelLoadException;
import ru.vsu.cs.cg.exception.handler.GlobalExceptionHandler;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.objreader.ObjReader;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

import static ru.vsu.cs.cg.utils.constants.MessageConstants.*;

public final class DefaultModelLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultModelLoader.class);
    private static final GlobalExceptionHandler EXCEPTION_HANDLER = GlobalExceptionHandler.getInstance();
    private static final String MODELS_BASE_PATH = "/models/default/";
    private static final Map<ModelType, String> MODEL_PATHS = new EnumMap<>(ModelType.class);

    public enum ModelType {
        CUBE("Куб"),
        CONE("Конус"),
        CYLINDER("Цилиндр"),
        TEAPOT("Чайник"),
        PLANE("Плоскость");

        private final String displayName;

        ModelType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    static {
        MODEL_PATHS.put(ModelType.CUBE, MODELS_BASE_PATH + "cube.obj");
        MODEL_PATHS.put(ModelType.CONE, MODELS_BASE_PATH + "cone.obj");
        MODEL_PATHS.put(ModelType.CYLINDER, MODELS_BASE_PATH + "cylinder.obj");
        MODEL_PATHS.put(ModelType.TEAPOT, MODELS_BASE_PATH + "teapot.obj");
        MODEL_PATHS.put(ModelType.PLANE, MODELS_BASE_PATH + "plane.obj");
    }

    public static Model loadModel(ModelType modelType) {
        final String path = MODEL_PATHS.get(modelType);

        if (path == null) {
            LOG.error("Неизвестный тип модели: {}", modelType);
            throw new ModelLoadException(UNKNOWN_MODEL_TYPE + " " + modelType);
        }

        LOG.debug("Начало загрузки модели '{}' по пути: {}", modelType.getDisplayName(), path);

        try (InputStream inputStream = DefaultModelLoader.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                LOG.error("Файл модели не найден: {}", path);
                throw new ModelLoadException(MODEL_FILE_NOT_FOUND + " " + path);
            }

            final String content = new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
            );

            LOG.debug("Содержимое файла прочитано, размер: {} символов", content.length());

            final Model model = ObjReader.read(content);

            LOG.info("Модель '{}' успешно загружена", modelType.getDisplayName());
            LOG.debug("Загружено вершин: {}, полигонов: {}",
                model.getVertices().size(),
                model.getPolygons().size());

            return model;

        } catch (IOException e) {
            LOG.error("Ошибка загрузки модели '{}': {}", modelType.getDisplayName(), e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ModelLoadException(MODEL_LOAD_ERROR + " " + modelType.getDisplayName(), e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка загрузки модели '{}': {}", modelType.getDisplayName(), e.getMessage(), e);
            EXCEPTION_HANDLER.handleExceptionWithCustomMessage(e, MODEL_LOAD_ERROR);
            throw new ModelLoadException(MODEL_LOAD_ERROR, e);
        }
    }

    public static ModelType[] getAvailableModels() {
        try {
            return ModelType.values();
        } catch (Exception e) {
            LOG.error("Ошибка получения доступных моделей: {}", e.getMessage(), e);
            EXCEPTION_HANDLER.handleException(e);
            return new ModelType[0];
        }
    }
}
