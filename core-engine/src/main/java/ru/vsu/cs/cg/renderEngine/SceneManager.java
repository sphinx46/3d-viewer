package ru.vsu.cs.cg.renderEngine;

import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import java.util.List;

public interface SceneManager {
    public void addModel(Model model);

    public List<Model> getModels();

    public void addCamera(Camera camera);
    public List<Camera> getCameras();

    public void setActiveCamera(int index);

    public Camera getActiveCamera();
}
