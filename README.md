# FastImage 0.1.4 [ALPHA-2026-09] — High-Performance Off-Heap Image Processing for Java

[![Status](https://img.shields.io/badge/status-0.1.4-brightgreen.svg)](https://github.com/andrestubbe/FastImage/releases/tag/0.1.4)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.4-green.svg)](https://jitpack.io/#andrestubbe/FastImage)

---

**⚡ 10–50× faster than Java's BufferedImage.** Off-heap zero-copy memory. SIMD AVX2 accelerated Bicubic spline, Area-Average Anti-Aliasing, and blur filters.

`FastImage` provides ultra-fast C++ native image processing for Java applications, replacing slow JVM `BufferedImage` rendering loops with SIMD-accelerated Catmull-Rom Bicubic scaling, Area-Average Anti-Aliasing, Dual-Kawase blur, and color transforms.

![Showcase](https://raw.githubusercontent.com/andrestubbe/FastImage/main/docs/screenshot.png)

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
- [Architecture & Pipeline](#architecture--pipeline)
- [Performance Benchmarks](#performance-benchmarks)
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

## Architecture & Pipeline

```
┌─────────────────────────────────────────────────────────────┐
│    Foreign Source (FastScreen / FastCamera / Raw Pointer)   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Zero-Copy wrap() / Direct Native Allocation
                               ▼
┌─────────────────────────────────────────────────────────────┐
│          Off-Heap Unmanaged Memory Buffer (32-Bit ARGB)     │
└──────────────────────────────┬──────────────────────────────┘
                               │ 256-Bit AVX2 SIMD Vector Kernels
                               │ (Dual-Kawase Blur / Bilinear / Area-Average)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│           Processed Off-Heap FastImage (0 Bytes GC)         │
└──────────────────────────────┬──────────────────────────────┘
                               │ Instant Chaining or Zero-Copy Export
                               ▼
┌─────────────────────────────────────────────────────────────┐
│         Vision Models (ONNX/Vulkan) or BufferedImage        │
└─────────────────────────────────────────────────────────────┘
```

---

## Performance Benchmarks

In the official [JMH Benchmark](examples/Benchmark), `FastImage` measured throughput for full 1080p (1920×1080) to 720p (1280×720) frame processing:

```text
Benchmark                                       Mode  Cnt    Score   Units
Benchmark.benchmarkFastImageResizeNearest      thrpt    2  642.614   ops/s
Benchmark.benchmarkFastImageResizeAreaAverage  thrpt    2  199.345   ops/s
Benchmark.benchmarkFastImageResizeBilinear     thrpt    2  127.117   ops/s
Benchmark.benchmarkFastImageResizeBicubic      thrpt    2   37.105   ops/s
Benchmark.benchmarkFastImageKawaseBlur         thrpt    2   24.451   ops/s
```

> [!NOTE]
> **Environment & Setup**: Measured on an 11th Gen Intel Core i5-1135G7 (Microsoft Surface Pro 8) running Windows 11 with JDK 21. `FastImage` processes full 1080p uncompressed frames at up to **640+ operations per second** (Point) and **199+ operations per second** (OpenMP-accelerated Area-Average Anti-Aliasing) with **zero JVM Garbage Collection allocations**.

---

## API Quick Reference

### Factory & Memory Wrapping
| Method | Description | Path |
|--------|-------------|------|
| `create(width, height)` | Allocates an unmanaged off-heap ARGB pixel buffer. | [Reference 📖](docs/REFERENCE.md#create) |
| `fromBufferedImage(image)` | Converts a Java `BufferedImage` to `FastImage`. | [Reference 📖](docs/REFERENCE.md#frombufferedimage) |
| `fromPixels(pixels, width, height)` | Creates an instance directly from an `int[]` array. | [Reference 📖](docs/REFERENCE.md#frompixels) |
| `wrap(address, width, height)` | Zero-copy wrap of raw native 64-bit address. | [Reference 📖](docs/REFERENCE.md#wrap) |
| `wrap(Pointer, width, height)` | Zero-copy wrap of a `FastPointer` handle. | [Reference 📖](docs/REFERENCE.md#wrap) |
| `wrap(ByteBuffer, width, height)` | Zero-copy wrap of a direct `java.nio.ByteBuffer`. | [Reference 📖](docs/REFERENCE.md#wrap) |

### Resampling & Geometry
| Method | Description | Path |
|--------|-------------|------|
| `resize(newW, newH)` | Native AVX2 SIMD bilinear image scaling. | [Reference 📖](docs/REFERENCE.md#resize) |
| `resizeNearest(newW, newH)` | Ultra-fast Nearest-Neighbor (point) scaling. | [Reference 📖](docs/REFERENCE.md#resizenearest) |
| `resizeBicubic(newW, newH)` | Ultra-sharp Catmull-Rom Bicubic spline resampling. | [Reference 📖](docs/REFERENCE.md#resizebicubic) |
| `resizeAreaAverage(newW, newH)` | OpenMP-accelerated Area-Average downsampler. | [Reference 📖](docs/REFERENCE.md#resizeareaaverage) |
| `crop(x, y, width, height)` | Crops sub-region into a new `FastImage`. | [Reference 📖](docs/REFERENCE.md#crop) |
| `flipHorizontal()` / `flipVertical()` | Flips image along X or Y axis in-place. | [Reference 📖](docs/REFERENCE.md#flip) |

### Convolutions & Blur Filters
| Method | Description | Path |
|--------|-------------|------|
| `blurKawase(radius, passes)` | High-speed multi-pass Kawase blur filter. | [Reference 📖](docs/REFERENCE.md#blurkawase) |
| `blurDualKawase(radius)` | Premium 2-pass Dual-Kawase down/upsample blur. | [Reference 📖](docs/REFERENCE.md#blurdualkawase) |
| `blurGaussian(radius)` | Smooth separable Gaussian blur. | [Reference 📖](docs/REFERENCE.md#blurgaussian) |
| `blurStack(radius)` | CSS `backdrop-filter` grade stack blur. | [Reference 📖](docs/REFERENCE.md#blurstack) |
| `blurBox(radius)` | Fast box blur filter. | [Reference 📖](docs/REFERENCE.md#blurbox) |
| `blurMipmapped(radius)` | Large-radius hierarchical down/upsample blur. | [Reference 📖](docs/REFERENCE.md#blurmipmapped) |

### Color Operations & Export
| Method | Description | Path |
|--------|-------------|------|
| `grayscale()` | Vectorized luminance weighting to monochrome. | [Reference 📖](docs/REFERENCE.md#grayscale) |
| `adjustBrightness(factor)` | Scales pixel luminance (`1.0` = normal). | [Reference 📖](docs/REFERENCE.md#brightness) |
| `adjustContrast(factor)` | Adjusts image contrast ratio. | [Reference 📖](docs/REFERENCE.md#contrast) |
| `toBufferedImage()` | Converts native pixels to standard `BufferedImage`. | [Reference 📖](docs/REFERENCE.md#tobufferedimage) |
| `getPixels(int[] dest)` | Fills a pre-allocated Java `int[]` array. | [Reference 📖](docs/REFERENCE.md#getpixels) |
| `getDirectBuffer()` | Returns a direct `ByteBuffer` view of native memory. | [Reference 📖](docs/REFERENCE.md#getdirectbuffer) |
| `getPointer()` | Returns a `fastpointer.Pointer` to native memory. | [Reference 📖](docs/REFERENCE.md#getpointer) |

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
        <version>0.1.4</version>
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
        <version>0.1.2</version>
    </dependency>

    <!-- FastPointer Address Wrapper -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastPointer</artifactId>
        <version>0.1.2</version>
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
    implementation 'com.github.andrestubbe:FastImage:0.1.4'
    implementation 'com.github.andrestubbe:FastSIMD:0.1.3'
    implementation 'com.github.andrestubbe:FastMemory:0.1.2'
    implementation 'com.github.andrestubbe:FastPointer:0.1.2'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the required JARs directly to add them to your classpath:

1. ⚡ **[FastImage-0.1.4.jar](https://github.com/andrestubbe/FastImage/releases/tag/0.1.4/FastImage-0.1.4.jar)** (The Core Library)
2. 🚀 **[FastSIMD-0.1.3.jar](https://github.com/andrestubbe/FastSIMD/releases/download/0.1.3/FastSIMD-0.1.3.jar)** (Hardware Vector Acceleration Engine)
3. 💾 **[FastMemory-0.1.2.jar](https://github.com/andrestubbe/FastMemory/releases/tag/0.1.2/FastMemory-0.1.2.jar)** (32-Byte Aligned Allocator)
4. 📍 **[FastPointer-0.1.2.jar](https://github.com/andrestubbe/FastPointer/releases/tag/0.1.2/FastPointer-0.1.2.jar)** (Primitive Address Pointer)
5. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be included in your classpath for the native SIMD JNI bindings to function correctly.

---

## Documentation

- **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.
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
