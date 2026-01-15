package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.camera.Camera;
import ru.vsu.cs.cg.scene.SceneManager;
import ru.vsu.cs.cg.utils.camera.CameraUtils;
import ru.vsu.cs.cg.utils.controller.UiFieldUtils;
import ru.vsu.cs.cg.utils.dialog.DialogManager;
import ru.vsu.cs.cg.utils.validation.InputValidator;

import java.util.Set;

public class CameraController {
    private static final Logger LOG = LoggerFactory.getLogger(CameraController.class);
    private static final double MIN_FOV = 1.0;
    private static final double MAX_FOV = 179.0;
    private static final float MIN_NEAR_PLANE = 0.001f;

    private SceneManager sceneManager;

    @FXML private ComboBox<String> cameraSelector;
    @FXML private Slider fovSlider;
    @FXML private TextField fovField;
    @FXML private TextField aspectRatioField;
    @FXML private TextField nearPlaneField;
    @FXML private TextField farPlaneField;

    @FXML private TextField cameraPosXField;
    @FXML private TextField cameraPosYField;
    @FXML private TextField cameraPosZField;

    @FXML private TextField cameraTargetXField;
    @FXML private TextField cameraTargetYField;
    @FXML private TextField cameraTargetZField;

    @FXML private Button createCameraButton;
    @FXML private Button deleteCameraButton;
    @FXML private Button applyCameraButton;
    @FXML private Button resetCameraButton;

    @FXML
    public void initialize() {
        LOG.debug("Инициализация CameraController");
        initializeBindings();
        initializeActions();
    }

    private void initializeBindings() {
        fovSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            fovField.setText(UiFieldUtils.formatDouble(newVal.doubleValue()));
            applyFovImmediate(newVal.doubleValue());
        });
    }

    private void initializeActions() {
        cameraSelector.setOnAction(event -> handleCameraSelection());

        createCameraButton.setOnAction(event -> createCamera(new Vector3f(0, 2, 5)));
        deleteCameraButton.setOnAction(event -> deleteCamera());

        applyCameraButton.setOnAction(event -> applySettings());
        resetCameraButton.setOnAction(event -> resetSettings());
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        refreshCameraList();
    }

    public void refreshCameraList() {
        if (sceneManager == null) return;

        String currentSelection = cameraSelector.getValue();
        cameraSelector.getItems().clear();

        for (Camera cam : sceneManager.getCameras()) {
            cameraSelector.getItems().add(cam.getId());
        }

        if (currentSelection != null && cameraSelector.getItems().contains(currentSelection)) {
            cameraSelector.setValue(currentSelection);
        } else if (sceneManager.getActiveCamera() != null) {
            cameraSelector.setValue(sceneManager.getActiveCamera().getId());
            loadCameraToFields(sceneManager.getActiveCamera());
        }
    }

    private void handleCameraSelection() {
        String selectedId = cameraSelector.getValue();
        if (selectedId == null) return;

        Camera selectedCam = sceneManager.getCameras().stream()
            .filter(c -> c.getId().equals(selectedId))
            .findFirst()
            .orElse(null);

        if (selectedCam != null) {
            sceneManager.setActiveCamera(selectedCam);
            loadCameraToFields(selectedCam);
        }
    }

    public void loadCameraToFields(Camera cam) {
        if (cam == null) return;

        if (cameraPosXField.isFocused() || cameraPosYField.isFocused() || cameraPosZField.isFocused() ||
                cameraTargetXField.isFocused() || cameraTargetYField.isFocused() || cameraTargetZField.isFocused() ||
                fovField.isFocused() || nearPlaneField.isFocused() || farPlaneField.isFocused() || aspectRatioField.isFocused()) {
            return;
        }

        double fovDeg = Math.toDegrees(cam.getFov());

        if (!fovSlider.isValueChanging()) {
            fovSlider.setValue(fovDeg);
        }

        fovField.setText(UiFieldUtils.formatDouble(fovDeg));

        aspectRatioField.setText(UiFieldUtils.formatDouble(cam.getAspectRatio()));
        nearPlaneField.setText(UiFieldUtils.formatDouble(cam.getNearPlane()));
        farPlaneField.setText(UiFieldUtils.formatDouble(cam.getFarPlane()));

        cameraPosXField.setText(formatFloat(cam.getPosition().getX()));
        cameraPosYField.setText(formatFloat(cam.getPosition().getY()));
        cameraPosZField.setText(formatFloat(cam.getPosition().getZ()));

        cameraTargetXField.setText(formatFloat(cam.getTarget().getX()));
        cameraTargetYField.setText(formatFloat(cam.getTarget().getY()));
        cameraTargetZField.setText(formatFloat(cam.getTarget().getZ()));
    }

    public void createCamera(Vector3f position) {
        if (sceneManager == null) return;

        Set<String> existingIds = sceneManager.getCameras().stream()
            .map(Camera::getId)
            .collect(java.util.stream.Collectors.toSet());

        String id = CameraUtils.generateUniqueCameraId(existingIds);
        Camera newCam = new Camera(id, position, new Vector3f(0, 0, 0));

        if (!sceneManager.getCameras().isEmpty()) {
            newCam.setAspectRatio(sceneManager.getActiveCamera().getAspectRatio());
        }

        sceneManager.addCamera(newCam);
        sceneManager.setActiveCamera(newCam);

        refreshCameraList();
        LOG.info("Создана новая камера: {}", newCam.getId());
    }

    private void deleteCamera() {
        Camera selectedCam = getSelectedCamera();
        if (selectedCam == null) return;

        if (sceneManager.getCameras().size() <= 1) {
            DialogManager.showInfo("Ошибка", "Невозможно удалить единственную камеру в сцене.");
            return;
        }

        try {
            sceneManager.removeCamera(selectedCam);
            refreshCameraList();
        } catch (Exception e) {
            DialogManager.showInfo("Ошибка удаления", e.getMessage());
        }
    }

    private void applySettings() {
        Camera cam = getSelectedCamera();
        if (cam == null) return;

        try {
            double fovDeg = parseDouble(fovField.getText());
            if (fovDeg < MIN_FOV || fovDeg > MAX_FOV) {
                throw new IllegalArgumentException("FOV должен быть от " + MIN_FOV + " до " + MAX_FOV);
            }
            cam.setFov((float) Math.toRadians(fovDeg));

            float aspect = (float) parseDouble(aspectRatioField.getText());
            float near = (float) parseDouble(nearPlaneField.getText());
            float far = (float) parseDouble(farPlaneField.getText());

            if (near < MIN_NEAR_PLANE) throw new IllegalArgumentException("Ближняя плоскость должна быть >= " + MIN_NEAR_PLANE);
            if (far <= near) throw new IllegalArgumentException("Дальняя плоскость должна быть больше ближней");

            cam.setAspectRatio(aspect);
            cam.setNearPlane(near);
            cam.setFarPlane(far);

            Vector3f pos = new Vector3f(
                (float) parseDouble(cameraPosXField.getText()),
                (float) parseDouble(cameraPosYField.getText()),
                (float) parseDouble(cameraPosZField.getText())
            );
            cam.setPosition(pos);

            Vector3f target = new Vector3f(
                (float) parseDouble(cameraTargetXField.getText()),
                (float) parseDouble(cameraTargetYField.getText()),
                (float) parseDouble(cameraTargetZField.getText())
            );
            cam.setTarget(target);

            LOG.info("Параметры камеры '{}' обновлены", cam.getId());

        } catch (Exception e) {
            DialogManager.showInfo("Некорректные данные", "Ошибка ввода: " + e.getMessage());
            resetSettings();
        }
    }

    private void resetSettings() {
        Camera cam = getSelectedCamera();
        if (cam != null) {
            loadCameraToFields(cam);
        }
    }

    private void applyFovImmediate(double fovDeg) {
        Camera cam = sceneManager.getActiveCamera();
        if (cam != null) {
            cam.setFov((float) Math.toRadians(fovDeg));
        }
    }

    private Camera getSelectedCamera() {
        String id = cameraSelector.getValue();
        if (id == null) return null;
        return sceneManager.getCameras().stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    private double parseDouble(String str) throws NumberFormatException {
        return InputValidator.parseDoubleSafe(str.replace(",", "."), 0.0);
    }

    private String formatFloat(float val) {
        return UiFieldUtils.formatDouble(val);
    }
}
