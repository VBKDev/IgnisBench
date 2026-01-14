package org.example.benchmark;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;

public class IgnisBench extends Application {

    private WritableImage buffer;
    private FireEngine engine;
    private Label statsLabel;
    private Label scoreLabel;

    // Default Settings
    private int width = 1280;
    private int height = 720;

    public enum TestMode {
        CPU_SINGLE,
        CPU_MULTI,
        GPU_STRESS
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #050505;"); // Deep Black Background

        // --- Display Area ---
        StackPane display = new StackPane();
        ImageView view = new ImageView();
        view.setPreserveRatio(true);
        view.setFitHeight(600);
        display.getChildren().add(view);
        root.setCenter(display);

        // --- Controls Sidebar ---
        VBox controls = new VBox(15);
        controls.setPadding(new Insets(20));
        controls.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);");
        controls.setPrefWidth(320);

        // Title
        Label lblTitle = new Label("IGNIS BENCH");
        lblTitle.setFont(Font.font("Segoe UI", 28));
        lblTitle.setStyle("-fx-text-fill: #ff3333; -fx-font-weight: bold;");

        // Mode Selector
        ComboBox<String> modeBox = new ComboBox<>();
        modeBox.getItems().addAll(
                "1. CPU Single Core",
                "2. CPU Multi Core (Max Throughput)",
                "3. GPU Stress (Heavy Effects)"
        );
        modeBox.getSelectionModel().select(1);
        modeBox.setMaxWidth(Double.MAX_VALUE);
        modeBox.setStyle("-fx-font-size: 14px;");

        // Resolution Selector
        ComboBox<String> resBox = new ComboBox<>();
        resBox.getItems().addAll(
                "Low (320x200)",
                "HD (1280x720)",
                "4K (3840x2160)",
                "8K (7680x4320)"
        );
        resBox.getSelectionModel().select(1);
        resBox.setMaxWidth(Double.MAX_VALUE);
        resBox.setStyle("-fx-font-size: 14px;");

        // Ultra Toggle
        CheckBox chkUltra = new CheckBox("ULTRA QUALITY\n(Smoke + Sparks + Blur)");
        chkUltra.setStyle("-fx-text-fill: #00aaff; -fx-font-weight: bold; -fx-font-size: 12px;");
        chkUltra.setTooltip(new Tooltip("Enables secondary physics simulation (Smoke) and particle sparks."));

        // Start Button
        Button btnStart = new Button("IGNITE");
        btnStart.setStyle("-fx-background-color: linear-gradient(to right, #d32f2f, #f44336); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.setPrefHeight(50);

        // Stats Labels
        statsLabel = new Label("Waiting for data...");
        statsLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-family: 'Consolas', monospace;");
        statsLabel.setMinHeight(100);
        statsLabel.setAlignment(Pos.TOP_LEFT);

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Consolas", 24));
        scoreLabel.setStyle("-fx-text-fill: white;");

        controls.getChildren().addAll(
                lblTitle,
                new Separator(),
                new Label("Mode"), modeBox,
                new Label("Resolution"), resBox,
                chkUltra,
                new Separator(),
                btnStart,
                new Separator(),
                statsLabel, scoreLabel
        );
        root.setRight(controls);

        // --- Logic: Start Button Action ---
        btnStart.setOnAction(e -> {
            // 1. Parse Resolution
            String res = resBox.getValue();
            if(res.contains("Low")) { width = 320; height = 200; }
            else if(res.contains("HD")) { width = 1280; height = 720; }
            else if(res.contains("4K")) { width = 3840; height = 2160; }
            else if(res.contains("8K")) { width = 7680; height = 4320; }

            // 2. Determine Mode
            TestMode mode = TestMode.CPU_SINGLE;
            if(modeBox.getValue().contains("Multi")) mode = TestMode.CPU_MULTI;
            if(modeBox.getValue().contains("GPU")) mode = TestMode.GPU_STRESS;

            // 3. Reset UI & Effects
            view.setImage(null);
            view.setEffect(null);

            // 4. Apply GPU Pipeline (Overkill Mode)
            if(mode == TestMode.GPU_STRESS) {
                ColorAdjust tonemap = new ColorAdjust();
                tonemap.setContrast(0.25);
                tonemap.setSaturation(0.8);

                Glow glow = new Glow(0.6);
                glow.setInput(tonemap);

                Bloom bloom = new Bloom(0.3);
                bloom.setInput(glow);

                GaussianBlur heatDistortion = new GaussianBlur(3.0);
                heatDistortion.setInput(bloom);

                MotionBlur speed = new MotionBlur(-90, 5.0);
                speed.setInput(heatDistortion);

                Reflection reflection = new Reflection();
                reflection.setTopOffset(0);
                reflection.setFraction(0.65);
                reflection.setTopOpacity(0.4);
                reflection.setBottomOpacity(0.0);
                reflection.setInput(speed);

                view.setEffect(reflection);
            }

            // 5. Start the Engine
            startBenchmark(view, mode, chkUltra.isSelected());
        });

        // --- Final Scene Setup ---
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("IgnisBench - Unleashed");

        // --- ICON LOADING ---
        // Reads Icon.png from resources
        try {
            InputStream iconStream = IgnisBench.class.getResourceAsStream("/Icon.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            } else {
                System.out.println("Warning: Icon.png not found in resources");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stage.setScene(scene);
        stage.show();
    }

    private void startBenchmark(ImageView view, TestMode mode, boolean ultra) {
        if (engine != null) engine.stop();

        buffer = new WritableImage(width, height);
        view.setImage(buffer);

        engine = new FireEngine(width, height, mode, ultra, buffer, statsLabel, scoreLabel);
        engine.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}