package ru.vsu.cs.cg.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.model.Model;
import ru.vsu.cs.cg.scene.SceneObject;
import ru.vsu.cs.cg.vertexremover.VertexRemover;
import ru.vsu.cs.cg.vertexremover.VertexRemoverImpl;
import ru.vsu.cs.cg.vertexremover.dto.VertexRemovalResult;
import ru.vsu.cs.cg.utils.dialog.DialogManager;

import java.util.HashSet;
import java.util.Set;

public class ModificationController extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(ModificationController.class);

    private final VertexRemover vertexRemover = new VertexRemoverImpl();

    @FXML private TextField vertexIndicesField;
    @FXML private CheckBox cleanUnusedCheckbox;
    @FXML private Button removeVerticesButton;
    @FXML private Button selectVerticesButton;

    @FXML private TextField polygonIndicesField;
    @FXML private Button removePolygonsButton;
    @FXML private Button selectPolygonsButton;

    @FXML private Label vertexCountLabel;
    @FXML private Label polygonCountLabel;
    @FXML private Label textureCountLabel;
    @FXML private Label normalCountLabel;

    @FXML
    public void initialize() {
        LOG.info("Инициализация контроллера модификации модели");

        setupButtonActions();
        setupTextFields();

        LOG.debug("Контроллер модификации модели успешно инициализирован");
    }

    private void setupButtonActions() {
        removeVerticesButton.setOnAction(event -> handleRemoveVertices());
        selectVerticesButton.setOnAction(event -> handleSelectVertices());

        removePolygonsButton.setOnAction(event -> handleRemovePolygons());
        selectPolygonsButton.setOnAction(event -> handleSelectPolygons());

        LOG.debug("Действия кнопок установлены");
    }

    private void setupTextFields() {
        vertexIndicesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9,\\-\\s]*")) {
                vertexIndicesField.setText(oldValue);
            }
        });

        polygonIndicesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9,\\-\\s]*")) {
                polygonIndicesField.setText(oldValue);
            }
        });

        LOG.debug("Валидация текстовых полей настроена");
    }

    private void handleRemoveVertices() {
        if (sceneController == null || !sceneController.hasSelectedObject()) {
            LOG.warn("Попытка удалить вершины при отсутствии выбранного объекта");
            DialogManager.showError("Выберите объект для модификации");
            return;
        }

        SceneObject selectedObject = sceneController.getSelectedObject();
        Model model = selectedObject.getModel();

        if (model == null) {
            LOG.warn("У выбранного объекта отсутствует модель");
            DialogManager.showError("У объекта нет модели для модификации");
            return;
        }

        Set<Integer> vertexIndices = parseIndices(vertexIndicesField.getText());
        if (vertexIndices.isEmpty()) {
            LOG.warn("Не указаны индексы вершин для удаления");
            DialogManager.showError("Введите индексы вершин для удаления");
            return;
        }

        try {
            LOG.info("Начало удаления вершин: объект={}, количество вершин={}, индексы={}",
                selectedObject.getName(), vertexIndices.size(), vertexIndices);

            boolean clearUnused = cleanUnusedCheckbox.isSelected();
            VertexRemovalResult result = vertexRemover.removeVertices(model, vertexIndices, clearUnused);

            sceneController.markModelModified();
            updateModelStatistics(model);

            LOG.info("Удаление вершин завершено успешно: удалено вершин={}, удалено полигонов={}",
                result.getRemovedVerticesCount(), result.getRemovedPolygonsCount());

            DialogManager.showSuccess(String.format("Удалено вершин: %d, полигонов: %d",
                result.getRemovedVerticesCount(), result.getRemovedPolygonsCount()));

        } catch (Exception e) {
            LOG.error("Ошибка удаления вершин: {}", e.getMessage(), e);
            DialogManager.showError("Ошибка удаления вершин: " + e.getMessage());
        }
    }

    private void handleSelectVertices() {
        LOG.debug("Выделение вершин: {}", vertexIndicesField.getText());
        DialogManager.showInfo("Выделение вершин",
            "Функция выделения вершин будет реализована в следующем обновлении");
    }

    private void handleRemovePolygons() {
        if (sceneController == null || !sceneController.hasSelectedObject()) {
            LOG.warn("Попытка удалить полигоны при отсутствии выбранного объекта");
            DialogManager.showError("Выберите объект для модификации");
            return;
        }

        SceneObject selectedObject = sceneController.getSelectedObject();
        Model model = selectedObject.getModel();

        if (model == null) {
            LOG.warn("У выбранного объекта отсутствует модель");
            DialogManager.showError("У объекта нет модели для модификации");
            return;
        }

        Set<Integer> polygonIndices = parseIndices(polygonIndicesField.getText());
        if (polygonIndices.isEmpty()) {
            LOG.warn("Не указаны индексы полигонов для удаления");
            DialogManager.showError("Введите индексы полигонов для удаления");
            return;
        }

        try {
            LOG.info("Начало удаления полигонов: объект={}, количество полигонов={}, индексы={}",
                selectedObject.getName(), polygonIndices.size(), polygonIndices);

            removePolygonsFromModel(model, polygonIndices);
            sceneController.markModelModified();
            updateModelStatistics(model);

            LOG.info("Удаление полигонов завершено успешно: удалено полигонов={}",
                polygonIndices.size());

            DialogManager.showSuccess(String.format("Удалено полигонов: %d", polygonIndices.size()));

        } catch (Exception e) {
            LOG.error("Ошибка удаления полигонов: {}", e.getMessage(), e);
            DialogManager.showError("Ошибка удаления полигонов: " + e.getMessage());
        }
    }

    private void handleSelectPolygons() {
        LOG.debug("Выделение полигонов: {}", polygonIndicesField.getText());
        DialogManager.showInfo("Выделение полигонов",
            "Функция выделения полигонов будет реализована в следующем обновлении");
    }

    private Set<Integer> parseIndices(String input) {
        Set<Integer> indices = new HashSet<>();

        if (input == null || input.trim().isEmpty()) {
            return indices;
        }

        try {
            String[] parts = input.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.contains("-")) {
                    String[] range = part.split("-");
                    if (range.length == 2) {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());
                        for (int i = start; i <= end; i++) {
                            indices.add(i);
                        }
                    }
                } else {
                    indices.add(Integer.parseInt(part));
                }
            }

            LOG.debug("Парсинг индексов завершен: входная строка='{}', результат={}", input, indices);
        } catch (NumberFormatException e) {
            LOG.error("Ошибка парсинга индексов: входная строка='{}', ошибка={}", input, e.getMessage());
            DialogManager.showError("Некорректный формат индексов. Используйте: 0,1,2 или 1-10");
        }

        return indices;
    }

    private void removePolygonsFromModel(Model model, Set<Integer> polygonIndices) {
        int originalCount = model.getPolygons().size();

        Set<Integer> validIndices = new HashSet<>();
        for (Integer index : polygonIndices) {
            if (index >= 0 && index < originalCount) {
                validIndices.add(index);
            }
        }

        model.getPolygons().removeIf(polygon -> validIndices.contains(model.getPolygons().indexOf(polygon)));

        LOG.debug("Полигоны удалены: запрошено={}, валидно={}, было={}, стало={}",
            polygonIndices.size(), validIndices.size(), originalCount, model.getPolygons().size());
    }

    public void updateUIFromSelectedObject() {
        if (sceneController == null || !sceneController.hasSelectedObject()) {
            resetUI();
            return;
        }

        SceneObject selectedObject = sceneController.getSelectedObject();
        Model model = selectedObject.getModel();

        if (model != null) {
            updateModelStatistics(model);
            enableModificationControls(true);
            LOG.debug("UI обновлен для объекта: {}", selectedObject.getName());
        } else {
            resetUI();
            LOG.debug("UI сброшен: у объекта нет модели");
        }
    }

    private void updateModelStatistics(Model model) {
        if (model != null) {
            vertexCountLabel.setText(String.valueOf(model.getVertices().size()));
            polygonCountLabel.setText(String.valueOf(model.getPolygons().size()));
            textureCountLabel.setText(String.valueOf(model.getTextureVertices().size()));
            normalCountLabel.setText(String.valueOf(model.getNormals().size()));

            LOG.trace("Статистика модели обновлена: вершин={}, полигонов={}",
                model.getVertices().size(), model.getPolygons().size());
        }
    }

    private void resetUI() {
        vertexIndicesField.clear();
        polygonIndicesField.clear();
        cleanUnusedCheckbox.setSelected(false);

        vertexCountLabel.setText("0");
        polygonCountLabel.setText("0");
        textureCountLabel.setText("0");
        normalCountLabel.setText("0");

        enableModificationControls(false);

        LOG.trace("UI контроллера модификации сброшен");
    }

    private void enableModificationControls(boolean enabled) {
        vertexIndicesField.setDisable(!enabled);
        polygonIndicesField.setDisable(!enabled);
        cleanUnusedCheckbox.setDisable(!enabled);
        removeVerticesButton.setDisable(!enabled);
        selectVerticesButton.setDisable(!enabled);
        removePolygonsButton.setDisable(!enabled);
        selectPolygonsButton.setDisable(!enabled);

        LOG.trace("Элементы управления модификацией {} для редактирования",
            enabled ? "включены" : "выключены");
    }
}
