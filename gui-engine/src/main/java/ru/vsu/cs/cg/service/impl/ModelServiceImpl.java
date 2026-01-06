package ru.vsu.cs.cg.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.exception.ApplicationException;
import ru.vsu.cs.cg.exception.FileOperationException;
import ru.vsu.cs.cg.exception.ModelLoadException;
import ru.vsu.cs.cg.exception.ValidationException;
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
            LOG.error("Ошибка загрузки стандартной модели '{}': {}",
                modelType.getDisplayName(), e.getMessage(), e);
            throw new ApplicationException(
                MessageConstants.MODEL_LOAD_ERROR + ": " + modelType.getDisplayName(), e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка при загрузке модели '{}': {}",
                modelType.getDisplayName(), e.getMessage(), e);
            throw new ApplicationException(
                MessageConstants.MODEL_LOAD_ERROR, e);
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
            LOG.error("Ошибка загрузки модели '{}': {}", filePath, e.getMessage(), e);
            throw new ApplicationException(
                MessageConstants.MODEL_LOAD_ERROR + ": " + filePath, e);
        } catch (Exception e) {
            LOG.error("Неожиданная ошибка при загрузке модели '{}': {}", filePath, e.getMessage(), e);
            throw new ApplicationException(
                MessageConstants.MODEL_LOAD_ERROR, e);
        }
    }

    private String readFileContent(String filePath) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            LOG.error("Ошибка чтения файла '{}': {}", filePath, e.getMessage(), e);
            throw new FileOperationException(
                MessageConstants.FILE_OPERATION_ERROR + ": " + filePath, e);
        }
    }

    @Override
    public Model createCustomObject() {
        LOG.info("Создание пользовательского объекта (куб)");

        try {
            return DefaultModelLoader.loadModel(DefaultModelLoader.ModelType.CUBE);

        } catch (Exception e) {
            LOG.error("Ошибка создания пользовательского объекта: {}", e.getMessage(), e);
            throw new ApplicationException(
                MessageConstants.MODEL_LOAD_ERROR, e);
        }
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

            String fileName = PathManager.getFileNameWithoutExtension(normalizedPath);
            LOG.debug("Сохранение файла с именем: {}", fileName);

            ObjWriter.write(normalizedPath, model);

            LOG.info("Модель успешно сохранена в файл: {}", normalizedPath);

        } catch (Exception e) {
            LOG.error("Ошибка сохранения модели в файл {}: {}", filePath, e.getMessage(), e);
            throw new FileOperationException(
                MessageConstants.OBJECT_SAVE_ERROR + ": " + filePath, e);
        }
    }

    private void validateModel(Model model) {
        try {
            InputValidator.validateNotNull(model, "Модель");

            if (model.getVertices().isEmpty()) {
                LOG.warn("Модель не содержит вершин");
            }

            if (model.getPolygons().isEmpty()) {
                LOG.warn("Модель не содержит полигонов");
            }
        } catch (IllegalArgumentException e) {
            LOG.error("Ошибка валидации модели: {}", e.getMessage(), e);
            throw new ValidationException(
                MessageConstants.VALIDATION_ERROR + ": модель", e);
        }
    }
}
