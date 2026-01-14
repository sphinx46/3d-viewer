package ru.vsu.cs.cg.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.cs.cg.exceptions.ModelLoadException;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.service.ModelService;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SceneServiceImplTest {

    @Mock
    private ModelService modelService;

    private SceneServiceImpl sceneService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        sceneService = new SceneServiceImpl(modelService);
    }

    @Test
    @DisplayName("Создание новой сцены должно возвращать сцену с корректным именем")
    void createNewScene_ShouldReturnSceneWithCorrectName() {
        Scene scene = sceneService.createNewScene();

        assertNotNull(scene);
        assertNotNull(scene.getName());
        assertTrue(scene.getName().startsWith("Сцена_"));
        assertEquals(0, scene.getObjectCount());
    }

    @Test
    @DisplayName("Загрузка сцены с пустым путем должно выбрасывать ModelLoadException")
    void loadScene_WithEmptyPath_ShouldThrowModelLoadException() {
        assertThrows(ModelLoadException.class, () -> sceneService.loadScene(""));
    }

    @Test
    @DisplayName("Загрузка сцены с несуществующим файлом должно выбрасывать ModelLoadException")
    void loadScene_WithNonExistentFile_ShouldThrowModelLoadException() {
        assertThrows(ModelLoadException.class, () -> sceneService.loadScene("nonexistent.json"));
    }

    @Test
    @DisplayName("Загрузка сцены из валидного JSON файла должно возвращать сцену")
    void loadScene_FromValidJsonFile_ShouldReturnScene() throws IOException {
        Path testFile = tempDir.resolve("test-scene.json");
        Files.writeString(testFile, """
            {
              "name": "TestScene",
              "objects": [],
              "selectedObjectId": null
            }
            """);

        Scene scene = sceneService.loadScene(testFile.toString());

        assertNotNull(scene);
        assertEquals("TestScene", scene.getName());
        assertEquals(0, scene.getObjectCount());
    }

    @Test
    @DisplayName("Сохранение сцены с нулевой сценой должно выбрасывать IllegalArgumentException")
    void saveScene_WithNullScene_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> sceneService.saveScene(null, "test.json"));
    }

    @Test
    @DisplayName("Сохранение сцены с пустым путем должно выбрасывать IllegalArgumentException")
    void saveScene_WithEmptyPath_ShouldThrowIllegalArgumentException() {
        Scene scene = new Scene();
        scene.setName("TestScene");

        assertThrows(IllegalArgumentException.class, () -> sceneService.saveScene(scene, ""));
    }

    @Test
    @DisplayName("Сохранение сцены должно работать даже если имя не установлено (Scene имеет default имя)")
    void saveScene_WithDefaultSceneName_ShouldWork() throws IOException {
        Scene scene = new Scene();
        Path tempFile = tempDir.resolve("test-scene.json");

        assertDoesNotThrow(() -> sceneService.saveScene(scene, tempFile.toString()));

        assertTrue(Files.exists(tempFile));

        Scene loadedScene = sceneService.loadScene(tempFile.toString());
        assertNotNull(loadedScene);
        assertNotNull(loadedScene.getName());
    }

    @Test
    @DisplayName("Сохранение сцены с установленным именем должно работать")
    void saveScene_WithValidName_ShouldWork() throws IOException {
        Scene scene = new Scene();
        scene.setName("TestScene");
        Path tempFile = tempDir.resolve("test-scene.json");

        assertDoesNotThrow(() -> sceneService.saveScene(scene, tempFile.toString()));

        assertTrue(Files.exists(tempFile));
    }

    @Test
    @DisplayName("Сохранение и загрузка сцены должно сохранять состояние")
    void saveAndLoadScene_ShouldPreserveState() throws IOException {
        Scene originalScene = new Scene();
        originalScene.setName("TestScene");

        Path saveFile = tempDir.resolve("saved-scene.json");
        sceneService.saveScene(originalScene, saveFile.toString());

        Scene loadedScene = sceneService.loadScene(saveFile.toString());

        assertNotNull(loadedScene);
        assertEquals(originalScene.getName(), loadedScene.getName());
        assertEquals(originalScene.getObjectCount(), loadedScene.getObjectCount());
    }

    @Test
    @DisplayName("Добавление стандартной модели должно создавать объект сцены")
    void addDefaultModelToScene_ShouldCreateSceneObject() {
        Scene scene = new Scene();
        Model model = new Model();

        when(modelService.loadDefaultModel(any(DefaultModelLoader.ModelType.class))).thenReturn(model);

        SceneObject result = sceneService.addDefaultModelToScene(scene, "CUBE");

        assertNotNull(result);
        assertEquals(1, scene.getObjectCount());
        verify(modelService).loadDefaultModel(DefaultModelLoader.ModelType.CUBE);
    }

    @Test
    @DisplayName("Добавление стандартной модели с неверным типом должно выбрасывать исключение")
    void addDefaultModelToScene_WithInvalidType_ShouldThrowException() {
        Scene scene = new Scene();

        assertThrows(Exception.class, () ->
            sceneService.addDefaultModelToScene(scene, "INVALID_TYPE")
        );
    }

    @Test
    @DisplayName("Удаление выбранного объекта должно уменьшать количество объектов")
    void removeSelectedObject_ShouldRemoveObject() {
        Scene scene = new Scene();
        Model model = new Model();
        SceneObject object = new SceneObject("Test", model);
        scene.addObject(object);
        scene.selectObject(object);

        sceneService.removeSelectedObject(scene);

        assertEquals(0, scene.getObjectCount());
        assertNull(scene.getSelectedObject());
    }

    @Test
    @DisplayName("Удаление объекта без выбора не должно вызывать исключение")
    void removeSelectedObject_WithoutSelection_ShouldNotThrow() {
        Scene scene = new Scene();
        Model model = new Model();
        scene.addObject(new SceneObject("Test", model));

        assertDoesNotThrow(() -> sceneService.removeSelectedObject(scene));
        assertEquals(1, scene.getObjectCount());
    }

    @Test
    @DisplayName("Дублирование выбранного объекта должно создавать копию")
    void duplicateSelectedObject_ShouldCreateCopy() {
        Scene scene = new Scene();
        Model model = new Model();
        SceneObject original = new SceneObject("TestObject", model);
        scene.addObject(original);
        scene.selectObject(original);

        sceneService.duplicateSelectedObject(scene);

        assertEquals(2, scene.getObjectCount());
    }

    @Test
    @DisplayName("Дублирование без выбора не должно создавать объекты")
    void duplicateSelectedObject_WithoutSelection_ShouldNotCreateObjects() {
        Scene scene = new Scene();
        Model model = new Model();
        scene.addObject(new SceneObject("Test", model));

        sceneService.duplicateSelectedObject(scene);

        assertEquals(1, scene.getObjectCount());
    }
}
