package ru.vsu.cs.cg.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.cs.cg.exceptions.ModelLoadException;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelServiceImplTest {

    @Mock
    private Model model;

    @InjectMocks
    private ModelServiceImpl modelService;

    @BeforeEach
    void setUp() {
        modelService = new ModelServiceImpl();
    }

    @Test
    @DisplayName("Загрузка стандартной модели должна возвращать модель")
    void loadDefaultModel_ShouldReturnModel() {
        try (MockedStatic<DefaultModelLoader> mocked = mockStatic(DefaultModelLoader.class)) {
            when(DefaultModelLoader.loadModel(any(DefaultModelLoader.ModelType.class)))
                .thenReturn(model);

            DefaultModelLoader.ModelType modelType = DefaultModelLoader.ModelType.CUBE;
            Model result = modelService.loadDefaultModel(modelType);

            assertNotNull(result);
            assertEquals(model, result);
        }
    }

    @Test
    @DisplayName("Загрузка стандартной модели с ошибкой должна выбрасывать исключение")
    void loadDefaultModel_WithException_ShouldThrow() {
        try (MockedStatic<DefaultModelLoader> mocked = mockStatic(DefaultModelLoader.class)) {
            when(DefaultModelLoader.loadModel(any(DefaultModelLoader.ModelType.class)))
                .thenThrow(new ModelLoadException("Test error"));

            DefaultModelLoader.ModelType modelType = DefaultModelLoader.ModelType.CUBE;

            assertThrows(ModelLoadException.class, () -> {
                modelService.loadDefaultModel(modelType);
            });
        }
    }

    @Test
    @DisplayName("Загрузка модели с неверным путем должна выбрасывать исключение")
    void loadModel_WithInvalidPath_ShouldThrowException() {
        assertThrows(ModelLoadException.class, () -> {
            modelService.loadModel("");
        });
    }

    @Test
    @DisplayName("Создание пользовательского объекта должно возвращать модель")
    void createCustomObject_ShouldReturnModel() {
        try (MockedStatic<DefaultModelLoader> mocked = mockStatic(DefaultModelLoader.class)) {
            when(DefaultModelLoader.loadModel(any(DefaultModelLoader.ModelType.class)))
                .thenReturn(model);

            Model result = modelService.createCustomObject();
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Сохранение модели с нулевой моделью должно выбрасывать исключение")
    void saveModelToFile_WithNullModel_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            modelService.saveModelToFile(null, "test.obj");
        });
    }

    @Test
    @DisplayName("Сохранение модели с пустым путем должно выбрасывать исключение")
    void saveModelToFile_WithEmptyPath_ShouldThrowException() {
        Model model = new Model();
        assertThrows(IllegalArgumentException.class, () -> {
            modelService.saveModelToFile(model, "");
        });
    }

    @Test
    @DisplayName("Сохранение модели с материалом с пустым именем материала должно выбрасывать исключение")
    void saveModelWithMaterial_WithEmptyMaterialName_ShouldThrowException() {
        Model model = new Model();
        assertThrows(IllegalArgumentException.class, () -> {
            modelService.saveModelWithMaterial(model, "test.obj", "", null, null, null, null, null);
        });
    }
}
