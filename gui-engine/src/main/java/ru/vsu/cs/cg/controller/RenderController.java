package ru.vsu.cs.cg.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.controller.enums.TransformationMode;
import ru.vsu.cs.cg.controller.handlers.MouseTransformationHandler;
import ru.vsu.cs.cg.renderEngine.camera.Camera;
import ru.vsu.cs.cg.scene.Scene;
import ru.vsu.cs.cg.scene.SceneManager;
import ru.vsu.cs.cg.utils.adapter.JavaFXPixelWriterAdapter;

public class RenderController {
    private static final Logger LOG = LoggerFactory.getLogger(RenderController.class);

    private final AnchorPane canvasContainer;
    private Canvas canvas;
    private AnimationTimer animationTimer;

    private final SceneManager sceneManager;
    private SceneController sceneController;

    private double lastMouseX;
    private double lastMouseY;

    public RenderController(AnchorPane canvasContainer) {
        this.canvasContainer = canvasContainer;
        this.sceneManager = new SceneManager();

        initializeCanvas();
        initializeTimeline();
        setupInputHandlers();
    }

    private void initializeCanvas() {
        this.canvas = new Canvas(800, 600);
        canvasContainer.getChildren().add(0, canvas); // Добавляем первым (на задний план)

        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());

        canvas.widthProperty().addListener((obs, oldVal, newVal) ->
            sceneManager.resize(newVal.intValue(), (int) canvas.getHeight()));
        canvas.heightProperty().addListener((obs, oldVal, newVal) ->
            sceneManager.resize((int) canvas.getWidth(), newVal.intValue()));

        sceneManager.resize((int) canvas.getWidth(), (int) canvas.getHeight());

        Camera camera = new Camera();
        sceneManager.addCamera(camera);
    }

    private void initializeTimeline() {
        this.animationTimer = new AnimationTimer() {
            private long lastRenderTime = 0;
            private final long TARGET_FPS = 40;
            private final long TARGET_INTERVAL = 1_000_000_000 / TARGET_FPS;

            @Override
            public void handle(long now) {
                if (now - lastRenderTime >= TARGET_INTERVAL) {
                    lastRenderTime = now;
                    render();
                }
            }
        };
    }

    private void render() {
        if (canvas == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        JavaFXPixelWriterAdapter pixelWriter = new JavaFXPixelWriterAdapter(gc.getPixelWriter());
        sceneManager.render(pixelWriter);
    }

    public void start() {
        animationTimer.start();
        LOG.info("Рендеринг запущен");
    }

    public void stop() {
        animationTimer.stop();
        LOG.info("Рендеринг остановлен");
    }

    public void setScene(Scene scene) {
        sceneManager.setScene(scene);
    }

    public void setSceneController(SceneController sceneController) {
        this.sceneController = sceneController;
        this.sceneManager.setScene(sceneController.getScene());
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    private void setupInputHandlers() {
        canvas.setFocusTraversable(true);

        // Клик мышкой для фокуса и обработка трансформаций
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            canvas.requestFocus();
            lastMouseX = event.getX();
            lastMouseY = event.getY();

            // Обработка трансформаций объектов
            if (sceneController != null && sceneController.hasSelectedObject()) {
                MouseTransformationHandler handler = sceneController.getMouseTransformationHandler();
                if (handler != null && handler.getCurrentMode() != TransformationMode.NONE) {
                    handler.handleMousePressed(event);
                    event.consume();
                }
            }
        });

        // Обработка движения мыши для трансформаций
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            // Обработка трансформаций объектов
            if (sceneController != null && sceneController.hasSelectedObject()) {
                MouseTransformationHandler handler = sceneController.getMouseTransformationHandler();
                if (handler != null && handler.isDragging()) {
                    handler.handleMouseDragged(event);
                    event.consume();
                }
            }
        });

        // Отпускание кнопки мыши
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            // Обработка трансформаций объектов
            if (sceneController != null && sceneController.hasSelectedObject()) {
                MouseTransformationHandler handler = sceneController.getMouseTransformationHandler();
                if (handler != null && handler.isDragging()) {
                    handler.handleMouseReleased(event);
                    event.consume();
                }
            }
        });

        // Зум колесиком (управление камерой - оставляю TODO)
        canvas.addEventHandler(ScrollEvent.SCROLL, event -> {
            Camera camera = sceneManager.getActiveCamera();
            if (camera == null) return;

            double delta = event.getDeltaY();
            // camera.zoom((float) delta);
            // TODO: Реализовать метод zoom в классе Camera
        });
    }
}
