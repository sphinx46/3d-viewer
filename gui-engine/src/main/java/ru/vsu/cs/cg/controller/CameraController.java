package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.math.Vector3f;
import ru.vsu.cs.cg.renderEngine.camera.Camera;
import ru.vsu.cs.cg.scene.SceneManager;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

import java.util.Set;

public class CameraController {
    private static final Logger LOG = LoggerFactory.getLogger(CameraController.class);
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
            fovField.setText(String.format("%.1f", newVal.doubleValue()));
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

    /**
     * Установка зависимости от SceneManager (вызывается из MainController)
     */
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        refreshCameraList();
    }

    /**
     * Обновление выпадающего списка камер
     */
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

    /**
     * Обработка выбора камеры в ComboBox
     */
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

    /**
     * Загрузка параметров камеры в UI поля
     */
    private void loadCameraToFields(Camera cam) {
        if (cam == null) return;

        // FOV: переводим радианы в градусы
        double fovDeg = Math.toDegrees(cam.getFov());
        fovSlider.setValue(fovDeg);
        fovField.setText(String.format("%.1f", fovDeg));

        // Параметры плоскостей
        aspectRatioField.setText(String.format("%.2f", cam.getAspectRatio()));
        nearPlaneField.setText(String.format("%.2f", cam.getNearPlane()));
        farPlaneField.setText(String.format("%.2f", cam.getFarPlane()));

        // Позиция
        cameraPosXField.setText(format(cam.getPosition().getX()));
        cameraPosYField.setText(format(cam.getPosition().getY()));
        cameraPosZField.setText(format(cam.getPosition().getZ()));

        // Цель
        cameraTargetXField.setText(format(cam.getTarget().getX()));
        cameraTargetYField.setText(format(cam.getTarget().getY()));
        cameraTargetZField.setText(format(cam.getTarget().getZ()));
    }

    /**
     * Создание новой камеры
     */
    public void createCamera(Vector3f position) {
        String id = generateUniqueCameraId();
        Camera newCam = new Camera(id,
            position,
            new Vector3f(0, 0, 0));

        newCam.setAspectRatio(sceneManager.getActiveCamera().getAspectRatio());

        sceneManager.addCamera(newCam);
        sceneManager.setActiveCamera(newCam);

        refreshCameraList();
        LOG.info("Создана новая камера: {}", newCam.getId());
    }

    /**
     * Удаление выбранной камеры
     */
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

    /**
     * Применение значений из полей к объекту камеры
     */
    private void applySettings() {
        Camera cam = getSelectedCamera();
        if (cam == null) return;

        try {
            double fovDeg = parseDouble(fovField.getText());
            if (fovDeg < 1 || fovDeg > 179) throw new IllegalArgumentException("FOV должен быть от 1 до 179");
            cam.setFov((float) Math.toRadians(fovDeg));

            float aspect = (float) parseDouble(aspectRatioField.getText());
            float near = (float) parseDouble(nearPlaneField.getText());
            float far = (float) parseDouble(farPlaneField.getText());

            if (near <= 0) throw new IllegalArgumentException("Ближняя плоскость должна быть > 0");
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

    /**
     * Сброс полей к текущим значениям камеры
     */
    private void resetSettings() {
        Camera cam = getSelectedCamera();
        if (cam != null) {
            loadCameraToFields(cam);
        }
    }

    // --- Вспомогательные методы ---

    /**
     * Мгновенное применение FOV (для слайдера)
     */
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
        try {
            return Double.parseDouble(str.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Не удалось прочитать число: " + str);
        }
    }

    /**
     * Вспомогательный метод для генерации уникального ID.
     * Перебирает имена Camera_1, Camera_2 и т.д., пока не найдет свободный слот.
     */
    private String generateUniqueCameraId() {
        Set<String> existingIds = sceneManager.getCameras().stream()
            .map(Camera::getId)
            .collect(java.util.stream.Collectors.toSet());

        int i = 1;
        while (true) {
            String potentialId = "Camera_" + i;
            if (!existingIds.contains(potentialId)) {
                return potentialId;
            }
            i++;
        }
    }

    private String format(float val) {
        return String.format("%.2f", val).replace(",", ".");
    }
}
