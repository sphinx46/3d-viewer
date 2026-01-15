package ru.vsu.cs.cg.utils.scene;

import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class SceneUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SceneUtils.class);
    private static final String COPY_SUFFIX = "_copy";

    private SceneUtils() {
    }

    public static String generateUniqueCopyName(String baseName, Scene scene) {
        String copyName = baseName + COPY_SUFFIX;
        int counter = 1;

        while (scene.findObjectByName(copyName).isPresent()) {
            copyName = baseName + COPY_SUFFIX + counter++;
        }

        return copyName;
    }

    public static void validateAndRenameObject(SceneObject object, String newName, Scene scene) {
        if (object == null) {
            LOG.warn("Попытка переименования null объекта");
            return;
        }

        if (newName == null || newName.trim().isEmpty()) {
            LOG.warn("Попытка переименовать объект с пустым именем");
            return;
        }

        String trimmedName = newName.trim();
        String currentName = object.getName();

        if (trimmedName.equals(currentName)) {
            LOG.debug("Имя объекта не изменилось: {}", currentName);
            return;
        }

        Optional<SceneObject> existingObject = scene.findObjectByName(trimmedName);
        if (existingObject.isPresent() && existingObject.get() != object) {
            LOG.warn("Объект с именем '{}' уже существует в сцене", trimmedName);
            throw new IllegalArgumentException("Объект с именем '" + trimmedName + "' уже существует в сцене");
        }

        object.setName(trimmedName);
        LOG.info("Объект переименован: '{}' -> '{}'", currentName, trimmedName);
    }
}
