package ru.vsu.cs.cg.service;

import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.utils.model.DefaultModelLoader;

public interface ModelService {
    Model loadDefaultModel(DefaultModelLoader.ModelType modelType);
    Model createCustomObject();
    void saveModelToFile(Model model, String filePath);
    Model loadModel(String filePath);
    void saveModelWithMaterial(Model model, String filePath, String materialName,
                                      String texturePath, float[] color, Float shininess,
                                      Float transparency, Float reflectivity);
}
