package ru.vsu.cs.cg.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vsu.cs.cg.renderEngine.PixelWriter;
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

            // Переменная для хранения времени последнего отрисованного кадра
            private long lastRenderTime = 0;

            // Целевой FPS (например, 60 кадров в секунду)
            private final long TARGET_FPS = 40;

            // Интервал в наносекундах (1 секунда = 1_000_000_000 нс)
            private final long TARGET_INTERVAL = 1_000_000_000 / TARGET_FPS;

            @Override
            public void handle(long now) {
                // Проверяем, прошло ли достаточно времени с последнего кадра
                if (now - lastRenderTime >= TARGET_INTERVAL) {

                    // Обновляем время последней отрисовки
                    lastRenderTime = now;

                    // Вызываем метод рендеринга
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

    /**
     * Устанавливает новую сцену для отрисовки.
     * Этот метод должен вызываться из SceneController при загрузке/создании файла.
     */
    public void setScene(Scene scene) {
        sceneManager.setScene(scene);
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    // --- Обработка ввода (Управление камерой) ---

    private void setupInputHandlers() {
        canvas.setFocusTraversable(true);

        // Клик мышкой для фокуса (чтобы работали горячие клавиши)
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            canvas.requestFocus();
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        // Вращение и перемещение
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            Camera camera = sceneManager.getActiveCamera();
            if (camera == null) return;

            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;

            if (event.getButton() == MouseButton.PRIMARY) {
                // ЛКМ: Вращение (Orbit)
                // camera.rotate((float) dx, (float) dy);
                // TODO: Реализовать метод rotate в классе Camera
            } else if (event.getButton() == MouseButton.MIDDLE || event.getButton() == MouseButton.SECONDARY) {
                // ПКМ или Колесо: Панорамирование (Pan)
                // camera.pan((float) dx, (float) dy);
                // TODO: Реализовать метод pan в классе Camera
            }

            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        // Зум колесиком
        canvas.addEventHandler(ScrollEvent.SCROLL, event -> {
            Camera camera = sceneManager.getActiveCamera();
            if (camera == null) return;

            double delta = event.getDeltaY();
            // camera.zoom((float) delta);
            // TODO: Реализовать метод zoom в классе Camera
        });
    }
}