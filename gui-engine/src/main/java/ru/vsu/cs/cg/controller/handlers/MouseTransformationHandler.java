package ru.vsu.cs.cg.controller.handlers;

import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.SceneController;
import ru.vsu.cs.cg.controller.enums.TransformationMode;
import ru.vsu.cs.cg.scene.Transform;
import ru.vsu.cs.cg.scene.SceneObject;

public class MouseTransformationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MouseTransformationHandler.class);

    private final SceneController sceneController;
    private TransformationMode currentMode = TransformationMode.NONE;
    private double lastMouseX;
    private double lastMouseY;
    private boolean isDragging = false;

    private static final double MOVE_SENSITIVITY = 0.02;
    private static final double ROTATE_SENSITIVITY = 0.1;
    private static final double SCALE_SENSITIVITY = 0.01;

    public MouseTransformationHandler(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    public void setTransformationMode(TransformationMode mode) {
        this.currentMode = mode;
    }

    public void handleMousePressed(MouseEvent event) {
        if (!sceneController.hasSelectedObject()) {
            return;
        }

        if (currentMode == TransformationMode.NONE) {
            return;
        }

        if (event.getButton() == MouseButton.PRIMARY) {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            isDragging = true;

            Cursor cursorType = getCursorForMode(currentMode);
            if (event.getSource() instanceof javafx.scene.Node) {
                javafx.scene.Node node = (javafx.scene.Node) event.getSource();
                node.getScene().setCursor(cursorType);
            }
        }
    }

    public void handleMouseDragged(MouseEvent event) {
        if (!isDragging || !sceneController.hasSelectedObject()) {
            return;
        }

        double deltaX = event.getX() - lastMouseX;
        double deltaY = event.getY() - lastMouseY;

        SceneObject selectedObject = sceneController.getSelectedObject();
        Transform transform = selectedObject.getTransform();

        switch (currentMode) {
            case MOVE:
                handleMove(deltaX, deltaY, transform, event.isShiftDown());
                break;
            case ROTATE:
                handleRotate(deltaX, deltaY, transform);
                break;
            case SCALE:
                handleScale(deltaY, transform);
                break;
        }

        lastMouseX = event.getX();
        lastMouseY = event.getY();

        sceneController.markModelModified();
        sceneController.updateUI();
    }

    public void handleMouseReleased(MouseEvent event) {
        if (isDragging) {
            isDragging = false;
            if (event.getSource() instanceof javafx.scene.Node) {
                javafx.scene.Node node = (javafx.scene.Node) event.getSource();
                node.getScene().setCursor(Cursor.DEFAULT);
            }
        }
    }

    private Cursor getCursorForMode(TransformationMode mode) {
        switch (mode) {
            case MOVE:
                return Cursor.MOVE;
            case ROTATE:
                return Cursor.HAND;
            case SCALE:
                return Cursor.V_RESIZE;
            default:
                return Cursor.DEFAULT;
        }
    }

    private void handleMove(double deltaX, double deltaY, Transform transform, boolean isZMode) {
        if (isZMode) {
            double newZ = transform.getPositionZ() + deltaY * MOVE_SENSITIVITY;
            transform.setPositionZ(newZ);
        } else {
            double newX = transform.getPositionX() - deltaX * MOVE_SENSITIVITY;
            double newY = transform.getPositionY() - deltaY * MOVE_SENSITIVITY;

            transform.setPositionX(newX);
            transform.setPositionY(newY);
        }
    }

    private void handleRotate(double deltaX, double deltaY, Transform transform) {
        double rotationX = transform.getRotationX() + deltaY * ROTATE_SENSITIVITY;
        double rotationY = transform.getRotationY() + deltaX * ROTATE_SENSITIVITY;

        transform.setRotationX(rotationX);
        transform.setRotationY(rotationY);
    }

    private void handleScale(double deltaY, Transform transform) {

        double scaleDelta = 1.0 - deltaY * SCALE_SENSITIVITY;

        double minScale = 0.1;
        double maxScale = 10.0;

        double currentScaleX = transform.getScaleX();
        double currentScaleY = transform.getScaleY();
        double currentScaleZ = transform.getScaleZ();

        double newScaleX = Math.max(minScale, Math.min(maxScale, currentScaleX * scaleDelta));
        double newScaleY = Math.max(minScale, Math.min(maxScale, currentScaleY * scaleDelta));
        double newScaleZ = Math.max(minScale, Math.min(maxScale, currentScaleZ * scaleDelta));

        transform.setScaleX(newScaleX);
        transform.setScaleY(newScaleY);
        transform.setScaleZ(newScaleZ);
    }

    private double normalizeAngle(double angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public TransformationMode getCurrentMode() {
        return currentMode;
    }

    public boolean isDragging() {
        return isDragging;
    }
}
