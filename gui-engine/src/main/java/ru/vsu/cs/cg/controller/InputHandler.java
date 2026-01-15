package ru.vsu.cs.cg.controller;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import ru.vsu.cs.cg.renderEngine.camera.Camera;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final SceneController sceneController;

    private double lastMouseX = -1;
    private double lastMouseY = -1;
    private boolean isRightMouseButtonDown = false;

    public InputHandler(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    // --- Events ---

    public void onKeyPressed(KeyEvent e) {
        activeKeys.add(e.getCode());
    }

    public void onKeyReleased(KeyEvent e) {
        activeKeys.remove(e.getCode());
    }

    public void onMousePressed(MouseEvent e) {
        // ПКМ для обзора
        if (e.isSecondaryButtonDown()) {
            isRightMouseButtonDown = true;
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        }
    }

    public void onMouseReleased(MouseEvent e) {
        if (!e.isSecondaryButtonDown()) {
            isRightMouseButtonDown = false;
        }
    }

    public void onMouseDragged(MouseEvent e) {
        // Вращаем голову только при зажатой ПКМ
        if (isRightMouseButtonDown) {
            Camera cam = sceneController.getActiveCamera();
            if (cam != null) {
                double dx = e.getSceneX() - lastMouseX;
                double dy = e.getSceneY() - lastMouseY;
                cam.rotate((float) dx, (float) dy);

                lastMouseX = e.getSceneX();
                lastMouseY = e.getSceneY();
            }
        }
    }

    public void onScroll(ScrollEvent e) {
        Camera cam = sceneController.getActiveCamera();
        if (cam != null) {
            float zoomDir = (float) e.getDeltaY() > 0 ? 1.0f : -1.0f;
            cam.moveRelative(zoomDir, 0, 0);
        }
    }

    // --- Update Loop ---

    public void update() {
        handleCameraMovement();
    }

    private void handleCameraMovement() {
        // WASD работает всегда
        Camera cam = sceneController.getActiveCamera();
        if (cam == null) return;

        float forward = 0;
        float right = 0;
        float up = 0;

        if (activeKeys.contains(KeyCode.W)) forward += 1;
        if (activeKeys.contains(KeyCode.S)) forward -= 1;
        if (activeKeys.contains(KeyCode.A)) right -= 1;
        if (activeKeys.contains(KeyCode.D)) right += 1;

        if (activeKeys.contains(KeyCode.SPACE)) up += 1;
        if (activeKeys.contains(KeyCode.SHIFT)) up -= 1;

        if (forward != 0 || right != 0 || up != 0) {
            cam.moveRelative(forward, right, up);
        }
    }
}