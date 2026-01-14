# IgnisBench

A CPU/GPU benchmarking tool that simulates fire physics to stress test your system's performance. Features real-time visualization of fire and smoke particle effects with multiple testing modes.



## Contributing

I'm open for updates and improvements! If you have bug fixes, features, or optimizations, please send a Pull Request.

### Development Setup

```bash
# Clone the repository
git clone <repository-url>
cd IgnisBench

# Run tests
./gradlew test

# Build and run
./gradlew build run
```

## License

This application is **100% free** and licensed under the MIT License.



## Features

- **Multiple Test Modes:**
  - **CPU Single Core**: Tests single-threaded performance
  - **CPU Multi Core**: Utilizes all available CPU cores for maximum throughput
  - **GPU Stress**: Heavy graphics effects pipeline for GPU testing

- **Resolution Options:** Low (320x200), HD (1280x720), 4K (3840x2160), 8K (7680x4320)
- **Ultra Quality Mode:** Enables secondary smoke physics simulation and particle effects
- **Real-time Performance Metrics:** CPU simulation FPS, GPU render FPS, memory usage
- **Score System:** MegaPixels per second (MP/s) performance metric

## Requirements

- **Java 17** or higher
- **JavaFX** runtime (included in the distribution)
- **Windows** (primary platform, may work on others)

## Building

This project uses Gradle. To build:

```bash
# Build the application
./gradlew build

# Create a fat JAR
./gradlew shadowJar

# Create a Windows executable
./gradlew createExe
```

## Running

### From Source
```bash
./gradlew run
```

### From JAR
```bash
java -jar build/libs/IgnisBench-1.0.jar
```

### Windows Executable
Run `build/launch4j/IgnisBench.exe` directly.

## Usage

1. Select your preferred **Test Mode** from the dropdown
2. Choose a **Resolution** (higher resolutions = more stress)
3. Enable **ULTRA QUALITY** for additional smoke effects (more CPU intensive)
4. Click **IGNITE** to start the benchmark
5. Monitor the real-time statistics in the sidebar
6. The score represents MegaPixels simulated per second

## How It Works

IgnisBench creates a real-time fire simulation using:
- **Fire Physics**: Pixel-based fire spread algorithm with decay and randomization
- **Smoke Physics**: Optional secondary particle system for ultra mode
- **GPU Pipeline**: JavaFX effects chain including bloom, glow, motion blur, and reflection
- **Multi-threading**: Parallel processing for CPU multi-core testing

The benchmark measures how many pixels can be simulated per second, providing a comprehensive stress test for both CPU and GPU performance.



> **Note to users:** Please don't try to sell this. I want to keep it free for everyone. The MIT license technically allows commercial use, but as the author, I really don't like that. Please keep it free. Anyway its too buggy to sell.

Copyright (c) 2026 @VeryBadKitsune/@VBKDev

## Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND. This benchmark may cause high CPU/GPU usage and is intended for testing purposes only. Monitor your system temperatures during extended testing.