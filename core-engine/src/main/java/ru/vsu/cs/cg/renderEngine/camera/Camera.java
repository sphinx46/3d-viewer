package ru.vsu.cs.cg.renderEngine.camera;

import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.GraphicConveyor;

public class Camera {
    private Vector3f position;
    private Vector3f target;
    private Vector3f up;

    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    private String id;

    public Camera(String id, Vector3f position, Vector3f target) {
        this.id = id;
        this.position = position;
        this.target = target;
        this.up = new Vector3f(0, 1, 0);

        this.fov = (float) Math.toRadians(60);
        this.aspectRatio = 1.77f;
        this.nearPlane = 0.1f;
        this.farPlane = 100f;
    }

    /**
     * Возвращает матрицу вида (View Matrix).
     * Переводит мир в систему координат камеры.
     */
    public Matrix4x4 getViewMatrix() {
        return GraphicConveyor.lookAt(position, target, up);
    }

    /**
     * Возвращает матрицу проекции (Projection Matrix).
     * Отвечает за перспективу.
     */
    public Matrix4x4 getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    // Геттеры и сеттеры для полей, чтобы можно было двигать камеру
    public void setPosition(Vector3f position) { this.position = position; }
    public Vector3f getPosition() { return position; }
    public void setTarget(Vector3f target) { this.target = target; }
    public Vector3f getTarget() { return target; }
    public void setAspectRatio(float ratio) { this.aspectRatio = ratio; }

    // Метод для перемещения (пример)
    public void move(Vector3f offset) {
        this.position = this.position.add(offset);
        this.target = this.target.add(offset); // Цель тоже двигается, если это панорамирование
    }
}
