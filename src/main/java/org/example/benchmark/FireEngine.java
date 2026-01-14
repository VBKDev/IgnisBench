package org.example.benchmark;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class FireEngine extends AnimationTimer {

    private final int width, height;
    private final IgnisBench.TestMode mode;
    private final boolean ultraMode;
    private final WritableImage buffer;
    private final Label stats, score;

    // Data
    private int[] firePixels;
    private byte[] smokePixels;
    private int[] palette;
    private int[] renderBuffer;

    // Threading Control
    private volatile boolean running = false;
    private Thread physicsThread;

    // Metrics
    private long physicsFrames = 0;
    private long renderFrames = 0;
    private long startTime;
    private long lastStatTime;
    private final Runtime runtime = Runtime.getRuntime();

    public FireEngine(int w, int h, IgnisBench.TestMode mode, boolean ultra, WritableImage buffer, Label stats, Label score) {
        this.width = w;
        this.height = h;
        this.mode = mode;
        this.ultraMode = ultra;
        this.buffer = buffer;
        this.stats = stats;
        this.score = score;

        this.renderBuffer = new int[width * height];
        createPalette();
        initFire();
    }

    private void createPalette() {
        palette = new int[37];
        for (int i = 0; i < 37; i++) {
            double t = i / 36.0;
            int r = (int) (Math.min(1.0, t * 2.5) * 255);
            int g = (int) (Math.min(1.0, Math.max(0, t * 2.5 - 0.8)) * 255);
            int b = (int) (Math.min(1.0, Math.max(0, t * 2.5 - 2.0)) * 255);
            palette[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }

    private void initFire() {
        firePixels = new int[width * height];
        if (ultraMode) smokePixels = new byte[width * height];
        // Seed
        for (int x = 0; x < width; x++) firePixels[(height - 1) * width + x] = 36;
    }

    @Override
    public void start() {
        running = true;
        startTime = System.nanoTime();
        lastStatTime = startTime;

        // 1. START PHYSICS THREAD
        // This thread loops as fast as the CPU allows, ignoring screen refresh rate.
        physicsThread = new Thread(this::physicsLoop);
        physicsThread.setDaemon(true); // Dies when app closes
        physicsThread.setName("Ignis-Physics-Core");
        physicsThread.start();

        // 2. START RENDER LOOP (GPU SYNC)
        super.start();
    }

    @Override
    public void stop() {
        running = false;
        super.stop();
        try {
            if(physicsThread != null) physicsThread.join(1000);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    // --- PHYSICS LOOP (CPU 100%) ---
    private void physicsLoop() {
        while (running) {
            // No fake math. Just pure simulation.
            // Since this is in a while(true) loop, it will eat 100% of cores available.

            if (mode == IgnisBench.TestMode.CPU_SINGLE) {
                calculateSingleThreaded();
            } else {
                calculateMultiThreaded();
            }
            physicsFrames++;
        }
    }

    // --- RENDER LOOP (Monitor Sync) ---
    @Override
    public void handle(long now) {
        // Take the current state of arrays and upload to GPU
        if (ultraMode) renderUltra();
        else renderStandard();

        renderFrames++;

        // Update Stats every 0.5 seconds
        if (now - lastStatTime >= 500_000_000) {
            updateStats(now);
            lastStatTime = now;
        }
    }

    private void updateStats(long now) {
        double elapsedSec = (now - startTime) / 1_000_000_000.0;

        // Physics FPS = How fast the CPU is suffering
        // Render FPS = How fast the Screen/GPU is drawing
        double physFps = physicsFrames / 0.5; // Since we update every 0.5s approx
        double rendFps = renderFrames / 0.5;

        long memUsage = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;

        // Score = Total MegaPixels Simulated
        double mpps = (physFps * width * height) / 1_000_000.0;

        Platform.runLater(() -> {
            stats.setText(String.format(
                    "CPU Sim Speed: %.0f FPS\nGPU Render:    %.0f FPS\nRAM Usage:     %d MB\nMode: %s",
                    physFps, rendFps, memUsage, (ultraMode ? "ULTRA" : "Std")
            ));
            score.setText(String.format("Score: %.0f MP/s", mpps));

            // Color Logic based on Physics Speed
            if(physFps > 100) stats.setStyle("-fx-text-fill: #00ff00; -fx-font-family: 'Consolas';");
            else stats.setStyle("-fx-text-fill: #ffaa00; -fx-font-family: 'Consolas';");
        });

        physicsFrames = 0;
        renderFrames = 0;
    }

    // --- CALCULATION LOGIC ---

    private void calculateSingleThreaded() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int x = 0; x < width; x++) {
            for (int y = 1; y < height; y++) {
                spreadFire(x, y, rand);
                if (ultraMode) spreadSmoke(x, y, rand);
            }
        }
    }

    private void calculateMultiThreaded() {
        // Parallel Streams will use all available cores
        IntStream.range(0, width).parallel().forEach(x -> {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            for (int y = 1; y < height; y++) {
                spreadFire(x, y, rand);
                if (ultraMode) spreadSmoke(x, y, rand);
            }
        });
    }

    private void spreadFire(int x, int y, ThreadLocalRandom rand) {
        int srcIdx = y * width + x;
        int pixelValue = firePixels[srcIdx];

        if (pixelValue == 0) {
            firePixels[(y - 1) * width + x] = 0;
        } else {
            int decay = rand.nextInt(3) & 3;
            int dstX = x - decay + 1;
            if (dstX >= width) dstX -= width;
            if (dstX < 0) dstX += width;

            int dstIdx = (y - 1) * width + dstX;
            firePixels[dstIdx] = Math.max(0, pixelValue - (decay & 1));

            if (ultraMode && pixelValue < 15 && pixelValue > 0 && rand.nextInt(10) == 0) {
                if (smokePixels[dstIdx] < 100) {
                    smokePixels[dstIdx] = (byte) (50 + rand.nextInt(50));
                }
            }
        }
    }

    private void spreadSmoke(int x, int y, ThreadLocalRandom rand) {
        int srcIdx = y * width + x;
        int density = Byte.toUnsignedInt(smokePixels[srcIdx]);
        if (density > 0) {
            int decay = 2;
            int wind = rand.nextInt(3) - 1;
            int dstX = x + wind;
            if (dstX >= width) dstX = width - 1;
            if (dstX < 0) dstX = 0;
            int dstY = y - 1;
            if (dstY < 0) dstY = 0;
            smokePixels[dstY * width + dstX] = (byte) Math.max(0, density - decay);
        }
    }

    // --- RENDER LOGIC ---

    private void renderStandard() {
        Arrays.parallelSetAll(renderBuffer, i -> palette[firePixels[i]]);
        buffer.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), renderBuffer, 0, width);
    }

    private void renderUltra() {
        // Ultra Rendering: Blending Fire + Smoke
        IntStream.range(0, width * height).parallel().forEach(i -> {
            int fireVal = firePixels[i];
            int smokeVal = Byte.toUnsignedInt(smokePixels[i]);

            if (fireVal > 0) {
                renderBuffer[i] = palette[fireVal];
            } else if (smokeVal > 5) {
                int alpha = Math.min(255, smokeVal * 2);
                renderBuffer[i] = (alpha << 24) | (0x33 << 16) | (0x33 << 8) | 0x33;
            } else {
                renderBuffer[i] = 0xFF000000;
            }
        });
        buffer.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), renderBuffer, 0, width);
    }
}