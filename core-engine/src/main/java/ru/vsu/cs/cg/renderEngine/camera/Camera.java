package ru.vsu.cs.cg.renderEngine.camera;

import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.GraphicConveyor;

/**
 * Класс камеры
 */
public class Camera {
    private final String id;
    private Vector3f position;
    private Vector3f target;
    private Vector3f up;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

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

    public Camera() {
        this("DefaultCamera_",
            new Vector3f(0, 2, 5),
            new Vector3f(0, 0, 0));
    }

    public Matrix4x4 getViewMatrix() {
        return GraphicConveyor.lookAt(position, target, up);
    }

    public Matrix4x4 getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    /**
     * Подсчет направления света
     */
    public Vector3f getLightDirection() {
        Vector3f dir = position.subtract(target);
        return dir.normalized();
    }

    public String getId() {
        return id;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }

    public Vector3f getUp() {
        return up;
    }

    public float getFov() {
        return fov;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }

    public void setUp(Vector3f up) {
        this.up = up;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
    }

    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
    }
}
