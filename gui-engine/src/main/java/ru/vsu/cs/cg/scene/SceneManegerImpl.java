package ru.vsu.cs.cg.scene;

import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.renderEngine.SceneManager;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import java.util.ArrayList;
import java.util.List;

public class SceneManegerImpl implements SceneManager {

    private String name;
    private List<SceneObject> objects;
    private SceneObject selectedObject;

    private List<Camera> cameras;
    private int activeCameraId;

    //public List<SceneObject> getObjects() { return new ArrayList<>(objects); }

    //public SceneObject getSelectedObject() { return selectedObject; }

//    public String getName() { return name; }
//    public void setName(String name) {
//        String oldName = this.name;
//        this.name = name != null ? name : "Новая сцена";
//        LOG.debug("Имя сцены изменено: '{}' -> '{}'", oldName, this.name);
//    }

    @Override
    public void addModel(Model model) {

    }

    @Override
    public List<Model> getModels() {
        return List.of();
    }

    @Override
    public void addCamera(Camera camera) {
        cameras.add(camera);
    }

    @Override
    public List<Camera> getCameras() {
        return cameras;
    }

    @Override
    public void setActiveCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            this.activeCameraId = index;
        }
    }

    @Override
    public Camera getActiveCamera() {
        return cameras.get(activeCameraId);    }
}
