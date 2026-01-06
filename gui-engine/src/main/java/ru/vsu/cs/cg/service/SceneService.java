package ru.vsu.cs.cg.service;

import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneObject;

import java.io.IOException;

public interface SceneService {
    Scene createNewScene();
    Scene loadScene(String filePath);
    void saveScene(Scene scene, String filePath) throws IOException;
    SceneObject addModelToScene(Scene scene, String modelFilePath);
    SceneObject addDefaultModelToScene(Scene scene, String modelType);
    void removeSelectedObject(Scene scene);
    void duplicateSelectedObject(Scene scene);
}
