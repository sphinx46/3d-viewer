package ru.vsu.cs.cg.renderEngine.camera;

import ru.vsu.cs.cg.math.Matrix4x4;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.GraphicConveyor;

public class Camera {
    private final String id;
    private Vector3f position;
    private Vector3f target;
    private Vector3f up;

    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    private float yaw = -90.0f;
    private float pitch = 0.0f;

    private final float mouseSensitivity = 0.15f;
    private final float movementSpeed = 0.5f;

    public Camera(String id, Vector3f position, Vector3f target) {
        this.id = id;
        this.position = position;
        this.target = target;
        this.up = new Vector3f(0, 1, 0); // World Up
        this.fov = (float) Math.toRadians(60);
        this.aspectRatio = 1.77f;
        this.nearPlane = 0.1f;
        this.farPlane = 100f;

        recalculateAngles();
    }

    public Camera() {
        this("DefaultCamera_0", new Vector3f(0, 2, 5), new Vector3f(0, 0, 0));
    }

    /**
     * Возвращает матрицу вида (view matrix) для текущей ориентации камеры.
     * Матрица преобразует мировые координаты в координаты вида (camera space).
     * Использует алгоритм LookAt для определения ориентации камеры.
     *
     * @return матрица 4x4 для преобразования вида
     */
    public Matrix4x4 getViewMatrix() {
        return GraphicConveyor.lookAt(position, target, up);
    }

    /**
     * Возвращает матрицу перспективной проекции.
     * Матрица преобразует координаты вида в координаты отсечения (clip space)
     * с учетом перспективного искажения.
     *
     * @return матрица 4x4 перспективной проекции
     * @see GraphicConveyor#perspective(float, float, float, float)
     */
    public Matrix4x4 getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    /**
     * Возвращает направление основного источника света.
     * По умолчанию направление света совпадает с направлением взгляда камеры
     * (от камеры к цели). Это удобно для простого освещения "от камеры".
     *
     * @return нормализованный вектор направления света
     */
    public Vector3f getLightDirection() {
        return position.subtract(target).normalizeSafe();
    }

    /**
     * Поворачивает камеру на основе смещений мыши.
     * Преобразует смещения пикселей в изменения углов рыскания и тангажа,
     * применяя чувствительность мыши и ограничивая угол тангажа.
     * После изменения углов автоматически обновляется вектор цели.
     *
     * @param xOffset смещение мыши по оси X (в пикселях)
     * @param yOffset смещение мыши по оси Y (в пикселях)
     */
    public void rotate(float xOffset, float yOffset) {
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;

        yaw += xOffset;
        pitch -= yOffset;

        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        updateTargetVector();
    }

    /**
     * Перемещает камеру относительно ее текущей ориентации.
     * Поддерживает движение вперед/назад, влево/вправо и вверх/вниз
     * в мировой системе координат. Объединяет вектора движения для
     * плавного перемещения по диагоналям.
     *
     * @param forward движение вперед (положительное) или назад (отрицательное)
     * @param right движение вправо (положительное) или влево (отрицательное)
     * @param upAmount движение вверх (положительное) или вниз (отрицательное)
     */
    public void moveRelative(float forward, float right, float upAmount) {
        Vector3f front = calculateFrontVector();

        Vector3f rightVec = front.cross(new Vector3f(0, 1, 0)).normalizeSafe();

        Vector3f worldUp = new Vector3f(0, 1, 0);

        Vector3f moveDir = new Vector3f(0, 0, 0);

        if (Math.abs(forward) > 1e-5) {
            moveDir = moveDir.add(front.multiply(forward));
        }
        if (Math.abs(right) > 1e-5) {
            moveDir = moveDir.subtract(rightVec.multiply(right));
        }
        if (Math.abs(upAmount) > 1e-5) {
            moveDir = moveDir.add(worldUp.multiply(upAmount));
        }

        if (moveDir.length() > 1e-5) {
            moveDir = moveDir.normalized().multiply(movementSpeed);

            this.position = this.position.add(moveDir);
            this.target = this.position.add(front);
        }
    }

    /**
     * Вычисляет фронтальный вектор камеры на основе углов рыскания и тангажа.
     * Использует сферические координаты для преобразования углов в
     * единичный вектор направления взгляда.
     *
     * @return нормализованный вектор направления взгляда камеры
     */
    private Vector3f calculateFrontVector() {
        float x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float y = (float) Math.sin(Math.toRadians(pitch));
        float z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        return new Vector3f(x, y, z).normalizeSafe();
    }

    /**
     * Обновляет точку цели (target) на основе текущей позиции и направления взгляда.
     * Вызывается автоматически после изменения углов ориентации камеры.
     */
    private void updateTargetVector() {
        Vector3f front = calculateFrontVector();
        this.target = this.position.add(front);
    }

    /**
     * Устанавливает новую позицию камеры и автоматически обновляет цель,
     * сохраняя текущее направление взгляда.
     *
     * @param position новая позиция камеры
     */
    public void setPosition(Vector3f position) {
        this.position = position;
        updateTargetVector();
    }

    /**
     * Устанавливает новую точку цели и пересчитывает углы ориентации.
     * Полезно для принудительного направления камеры на конкретный объект.
     *
     * @param target новая точка, на которую будет направлена камера
     */
    public void setTarget(Vector3f target) {
        this.target = target;
        recalculateAngles();
    }

    /**
     * Пересчитывает углы рыскания (yaw) и тангажа (pitch) на основе
     * текущих позиции и цели камеры. Используется при инициализации
     * или принудительном изменении цели.
     */
    private void recalculateAngles() {
        Vector3f direction = target.subtract(position).normalizeSafe();
        if (direction.length() < 1e-5) return;

        this.pitch = (float) Math.toDegrees(Math.asin(direction.getY()));
        this.yaw = (float) Math.toDegrees(Math.atan2(direction.getZ(), direction.getX()));
    }

    public String getId() { return id; }
    public Vector3f getPosition() { return position; }
    public Vector3f getTarget() { return target; }
    public Vector3f getUp() { return up; }
    public float getFov() { return fov; }
    public float getAspectRatio() { return aspectRatio; }
    public float getNearPlane() { return nearPlane; }
    public float getFarPlane() { return farPlane; }

    public void setUp(Vector3f up) { this.up = up; }
    public void setFov(float fov) { this.fov = fov; }
    public void setAspectRatio(float aspectRatio) { this.aspectRatio = aspectRatio; }
    public void setNearPlane(float nearPlane) { this.nearPlane = nearPlane; }
    public void setFarPlane(float farPlane) { this.farPlane = farPlane; }
}