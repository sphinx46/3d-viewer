package ru.vsu.cs.cg.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SceneManager {
    private static final Logger LOG = LoggerFactory.getLogger(SceneManager.class);

    private Scene scene;

    // todo: добавить список к амер
    // todo: добавить активную камеру
    // todo: добавить систему освещения
    // todo: добавить RenderEngine
    // todo: добавить ZBuffer
    // todo: добавить кэш текстур
    // todo: добавить настройки рендеринга

    public SceneManager() {
        this.scene = new Scene();
        LOG.info("SceneManager создан");
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        LOG.info("Сцена установлена в SceneManager: {}", scene.getName());
    }

    // todo: метод для получения видимых объектов
    // todo: метод для обновления состояния сцены
    // todo: метод для рендеринга сцены (будет вызывать RenderEngine)
    // todo: методы для работы с камерами
    // todo: методы для работы с освещением

    public void clearScene() {
        scene.clear();
        LOG.info("Сцена очищена через SceneManager");
    }

    public int getObjectCount() {
        return scene.getObjectCount();
    }

    public boolean isEmpty() {
        return scene.isEmpty();
    }

    public void resize(int width, int height) {
        LOG.debug("SceneManager получил запрос на изменение размера: {}x{}", width, height);
        // todo: обновить параметры камеры при изменении размера
        // todo: обновить RenderEngine/ZBuffer при изменении размера
    }
}
