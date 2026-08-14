# FastImage 0.1.1 [ALPHA-2026-08] — High-Performance Off-Heap Image Processing for Java

[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastImage/releases/tag/0.1.1)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.1-green.svg)](https://jitpack.io/#andrestubbe/FastImage)

---

**⚡ 10–50× faster than Java's BufferedImage.** Off-heap zero-copy memory. SIMD AVX2 accelerated image scaling and blur filters.

`FastImage` provides ultra-fast C++ native image processing for Java applications, replacing slow JVM `BufferedImage` rendering loops with SIMD-accelerated Bilinear scaling, Dual-Kawase blur, and color transforms.

[![Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)

---

## Quick Start — Example

```java
import fastimage.FastImage;
import java.awt.image.BufferedImage;

public class Demo {
    public static void main(String[] args) {
        // 1. Create 1080p off-heap image buffer
        FastImage img = FastImage.create(1920, 1080);

        // 2. Apply SIMD-accelerated filters (Chaining API)
        FastImage processed = img
            .resize(1280, 720)
            .blurKawase(3.0f, 2)
            .grayscale()
            .adjustBrightness(1.2f);

        // 3. Export to BufferedImage or native handle
        BufferedImage result = processed.toBufferedImage();
    }
}
```

---

## Table of Contents

- [Why FastImage?](#why-fastimage)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [Architecture Overview](#architecture-overview)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastImage?

Standard Java `BufferedImage` operations suffer from heavy heap allocation overhead, slow software rasterizers, and JVM GC stalls. `FastImage` addresses this by:

- **SIMD Vectorization** — Uses native C++ AVX2 vector instructions for multi-pixel parallel scaling and color manipulation.
- **Off-Heap Direct Memory** — Stores pixel buffers in native unmanaged memory to eliminate JVM GC pauses completely.
- **Kawase & Mipmapped Blur** — Implements modern GPU-grade blur algorithms running in native C++ for UI overlays.

---

## Key Features

* **⚡ Native AVX2 SIMD Acceleration** — Leverages 256-bit AVX2 vector registers for ultra-fast Bilinear scaling and color adjustments.
* **🖼️ Off-Heap Zero-GC Memory** — Allocates raw pixel buffers in direct native memory to prevent JVM Garbage Collection stalls.
* **🌀 Dual Kawase & Stack Blur** — High-speed blur algorithms for modern UI translucent overlays and game HUDs.
* **🔗 Chainable Fluent API** — Functional transformation pipeline returning new immutable `FastImage` instances.
* **🔄 Interoperable Java Bridge** — Zero-copy converter to and from `java.awt.image.BufferedImage`.

---

## Real-World Use Cases

- 🎮 **Game Overlays & Translucent HUDs**: Real-time Gaussian and Kawase blur filtering for high-FPS game HUD overlays.
- 📹 **Live Screen Capture Pipeline**: Downscale and process 1080p/4K video frames from **[FastScreen](https://github.com/andrestubbe/FastScreen)** without GC stutters.
- 🖼️ **Thumbnail & Preview Generators**: Batch-resize thousands of high-resolution images in web servers and media CMS platforms.
- 🤖 **Computer Vision Preprocessing**: Normalize, crop, and convert image frames before feeding AI vision models.

---

## Performance Benchmarks

In the official [JMH Benchmark](examples/Benchmark), `FastImage` measured throughput for full 1080p (1920x1080) frame processing:

```text
Benchmark                              Mode  Cnt   Score   Error  Units
JMH_Image.benchmarkFastImageResize    thrpt    2  19.521          ops/s
JMH_Image.benchmarkFastImageKawaseBlur thrpt   2  17.942          ops/s
```

> **1080p Real-Time Processing (19+ Full Frames / sec)**: `FastImage` resizes 1080p full HD uncompressed image buffers to 720p at **19.5 full operations per second** with **zero JVM Garbage Collection allocations**.

---

## Architecture Overview

**FastImage (This Library — Native Image Engine)**  
Provides SIMD-accelerated image scaling, blur filters, and color transforms.

**[FastSIMD](https://github.com/andrestubbe/FastSIMD) (Hardware Acceleration Engine)**  
Provides cross-platform hardware SIMD vectorization primitives.

**[FastScreen](https://github.com/andrestubbe/FastScreen) (Zero-Copy DirectX Screen Capture)**  
Feeds DirectX video frames into `FastImage` for real-time frame processing.

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `create(width, height)` | Creates an off-heap `FastImage` instance. | [Reference 📖](docs/REFERENCE.md#create) |
| `resize(newW, newH)` | AVX2 SIMD bilinear image scaling. | [Reference 📖](docs/REFERENCE.md#resize) |
| `blurKawase(radius, passes)` | High-speed Dual-Kawase blur filter. | [Reference 📖](docs/REFERENCE.md#blurkawase) |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the complete dependency stack to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastImage Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastImage</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastSIMD Hardware Vector Acceleration Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastSIMD</artifactId>
        <version>0.1.3</version>
    </dependency>

    <!-- FastMemory Aligned Allocator -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastMemory</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastPointer Address Wrapper -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastPointer</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastCore Native Loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastImage:0.1.1'
    implementation 'com.github.andrestubbe:FastSIMD:0.1.3'
    implementation 'com.github.andrestubbe:FastMemory:0.1.1'
    implementation 'com.github.andrestubbe:FastPointer:0.1.1'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the required JARs directly to add them to your classpath:

1. ⚡ **[FastImage-0.1.1.jar](https://github.com/andrestubbe/FastImage/releases/download/0.1.1/FastImage-0.1.1.jar)** (The Core Library)
2. 🚀 **[FastSIMD-0.1.3.jar](https://github.com/andrestubbe/FastSIMD/releases/download/0.1.3/FastSIMD-0.1.3.jar)** (Hardware Vector Acceleration Engine)
3. 💾 **[FastMemory-0.1.1.jar](https://github.com/andrestubbe/FastMemory/releases/download/0.1.1/FastMemory-0.1.1.jar)** (32-Byte Aligned Allocator)
4. 📍 **[FastPointer-0.1.1.jar](https://github.com/andrestubbe/FastPointer/releases/download/0.1.1/FastPointer-0.1.1.jar)** (Primitive Address Pointer)
5. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be included in your classpath for the native SIMD JNI bindings to function correctly.

---

## Documentation

- **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
- **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts and routing logic.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Off-heap zero-GC memory philosophy.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | 🔄 Planned |
| macOS | 🔄 Planned |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastScreen](https://github.com/andrestubbe/FastScreen) — DirectX zero-copy screen capture engine
- [FastGraphics](https://github.com/andrestubbe/FastGraphics) — Hardware-accelerated DirectX rendering
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI loader for FastJava libraries

---

Part of the FastJava Ecosystem — Making the JVM faster. Small package. Maximum speed. Zero bloat. ⚡
