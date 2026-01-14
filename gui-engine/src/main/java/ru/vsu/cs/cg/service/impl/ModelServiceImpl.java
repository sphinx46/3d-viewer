package ru.vsu.cs.cg.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exceptions.ModelLoadException;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.objreader.ObjReader;
import ru.vsu.cs.cg.objwriter.ObjWriter;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;
import ru.vsu.cs.cg.utils.validation.InputValidator;
import ru.vsu.cs.cg.utils.constants.MessageConstants;
import ru.vsu.cs.cg.utils.file.PathManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ModelServiceImpl implements ModelService {

    private static final Logger LOG = LoggerFactory.getLogger(ModelServiceImpl.class);

    @Override
    public Model loadDefaultModel(DefaultModelLoader.ModelType modelType) {
        LOG.info("Загрузка стандартной модели: {}", modelType.getDisplayName());

        try {
            return DefaultModelLoader.loadModel(modelType);
        } catch (ModelLoadException e) {
            LOG.error("Ошибка загрузки стандартной модели '{}': {}", modelType.getDisplayName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка при загрузке модели '{}': {}", modelType.getDisplayName(), e.getMessage());
            throw new ModelLoadException(MessageConstants.MODEL_LOAD_ERROR, e);
        }
    }

    @Override
    public Model loadModel(String filePath) {
        LOG.info("Загрузка модели, путь: {}", filePath);

        try {
            InputValidator.validateNotEmpty(filePath, "Путь к файлу модели");
            PathManager.validatePathForRead(filePath);

            String fileContent = readFileContent(filePath);
            return ObjReader.read(fileContent);
        } catch (ModelLoadException e) {
            LOG.error("Ошибка загрузки модели '{}': {}", filePath, e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка при загрузке модели '{}': {}", filePath, e.getMessage());
            throw new ModelLoadException(MessageConstants.MODEL_LOAD_ERROR, e);
        }
    }

    private String readFileContent(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    @Override
    public Model createCustomObject() {
        LOG.info("Создание пользовательского объекта (куб)");
        return loadDefaultModel(DefaultModelLoader.ModelType.CUBE);
    }

    @Override
    public void saveModelToFile(Model model, String filePath) {
        LOG.info("Сохранение модели в файл: {}", filePath);

        try {
            validateModel(model);
            InputValidator.validateNotEmpty(filePath, "Путь к файлу");

            String normalizedPath = PathManager.normalizePath(filePath);
            normalizedPath = PathManager.ensureExtension(normalizedPath, ".obj");
            PathManager.validatePathForSave(normalizedPath);

            ObjWriter.write(normalizedPath, model);
            LOG.info("Модель успешно сохранена в файл: {}", normalizedPath);

        } catch (Exception e) {
            LOG.error("Ошибка сохранения модели в файл {}: {}", filePath, e.getMessage());
            throw e;
        }
    }

    @Override
    public void saveModelWithMaterial(Model model, String filePath, String materialName,
                                      String texturePath, float[] color, Float shininess,
                                      Float transparency, Float reflectivity) {
        LOG.info("Сохранение модели с материалом '{}' в файл: {}", materialName, filePath);

        try {
            validateModel(model);
            InputValidator.validateNotEmpty(filePath, "Путь к файлу");
            InputValidator.validateNotEmpty(materialName, "Имя материала");

            String normalizedPath = PathManager.normalizePath(filePath);
            normalizedPath = PathManager.ensureExtension(normalizedPath, ".obj");
            PathManager.validatePathForSave(normalizedPath);

            ObjWriter.write(normalizedPath, model, materialName, texturePath,
                color, shininess, transparency, reflectivity);

            LOG.info("Модель с материалом '{}' успешно сохранена в файл: {}", materialName, normalizedPath);

        } catch (Exception e) {
            LOG.error("Ошибка сохранения модели с материалом в файл {}: {}", filePath, e.getMessage());
            throw e;
        }
    }

    private void validateModel(Model model) {
        InputValidator.validateNotNull(model, "Модель");

        if (model.getVertices().isEmpty()) {
            LOG.warn("Модель не содержит вершин");
        }

        if (model.getPolygons().isEmpty()) {
            LOG.warn("Модель не содержит полигонов");
        }
    }
}
