package ru.vsu.cs.cg.scene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Scene {
    private static final Logger LOG = LoggerFactory.getLogger(Scene.class);

    private final String id;
    private String name;
    private final List<SceneObject> objects;
    private SceneObject selectedObject;

    @JsonCreator
    public Scene(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("objects") List<SceneObject> objects,
        @JsonProperty("selectedObjectId") String selectedObjectId) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name != null ? name : "Новая сцена";
        this.objects = objects != null ? new ArrayList<>(objects) : new ArrayList<>();
        this.selectedObject = null;

        if (selectedObjectId != null) {
            findObjectById(selectedObjectId).ifPresent(obj -> this.selectedObject = obj);
        }

        LOG.debug("Создана сцена: id={}, name={}, объектов={}", this.id, this.name, this.objects.size());
    }

    public Scene() {
        this(UUID.randomUUID().toString(), "Новая сцена", new ArrayList<>(), null);
    }

    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) {
        String oldName = this.name;
        this.name = name != null ? name : "Новая сцена";
        LOG.debug("Имя сцены изменено: '{}' -> '{}'", oldName, this.name);
    }

    public List<SceneObject> getObjects() { return new ArrayList<>(objects); }

    public SceneObject getSelectedObject() { return selectedObject; }

    public Optional<SceneObject> findObjectById(String id) {
        if (id == null) return Optional.empty();
        return objects.stream()
            .filter(obj -> id.equals(obj.getId()))
            .findFirst();
    }

    public Optional<SceneObject> findObjectByName(String name) {
        if (name == null) return Optional.empty();
        return objects.stream()
            .filter(obj -> name.equals(obj.getName()))
            .findFirst();
    }

    public void addObject(SceneObject object) {
        if (object == null) {
            LOG.warn("Попытка добавить null объект в сцену");
            return;
        }

        objects.add(object);
        LOG.info("Объект '{}' добавлен в сцену '{}'. Всего объектов: {}",
            object.getName(), name, objects.size());
    }

    public boolean removeObject(SceneObject object) {
        if (object == null) return false;

        boolean removed = objects.remove(object);
        if (removed) {
            LOG.info("Объект '{}' удален из сцены '{}'. Осталось объектов: {}",
                object.getName(), name, objects.size());

            if (object.equals(selectedObject)) {
                selectedObject = null;
                LOG.debug("Выделенный объект сброшен");
            }
        }
        return removed;
    }

    public boolean removeObjectById(String id) {
        return findObjectById(id)
            .map(this::removeObject)
            .orElse(false);
    }

    public void selectObject(SceneObject object) {
        if (object != null && !objects.contains(object)) {
            LOG.warn("Попытка выбрать объект '{}', которого нет в сцене", object.getName());
            return;
        }

        SceneObject previous = selectedObject;
        selectedObject = object;

        LOG.debug("Выбор объекта изменен: {} -> {}",
            previous != null ? previous.getName() : "null",
            object != null ? object.getName() : "null");
    }

    public void selectObjectById(String id) {
        findObjectById(id).ifPresentOrElse(
            this::selectObject,
            () -> LOG.warn("Объект с id='{}' не найден в сцене", id)
        );
    }

    public void clearSelection() {
        selectedObject = null;
        LOG.debug("Выбор объекта сброшен");
    }

    public void clear() {
        int size = objects.size();
        objects.clear();
        selectedObject = null;
        LOG.info("Сцена '{}' очищена. Удалено объектов: {}", name, size);
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public int getObjectCount() {
        return objects.size();
    }

    @Override
    public String toString() {
        return String.format("Scene{id='%s', name='%s', objects=%d, selected=%s}",
            id, name, objects.size(),
            selectedObject != null ? selectedObject.getName() : "null");
    }
}
