# FastImage  SIMD-Accelerated, Off-Heap Image Processing for Java


[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastImage/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

**Ultra-fast native image processing using AVX2/SSE4.1 kernels and zero-GC memory management.**

FastImage ist eine ultra-schnelle, native-beschleunigte Image-Processing-Engine fr Java, gebaut fr das
FastJava-Ecosystem. Es kombiniert AVX/SSE SIMD, off-heap Storage, zero-copy Pipelines und eine fluent API, um typische
BufferedImage-Operationen **1050 schneller** auszufhren  ohne GC-Pressure, ohne Pixel-Loops, ohne JVM-Overhead.

```java
// Quick Start  SIMD-Accelerated Filtering

import fastimage.FastImage;

public class Demo {
    public static void main(String[] args) {
        FastImage img = FastImage.load("input.jpg");

        img.adjustContrast(1.2f)
                .blurStack(15.0f)
                .grayscale();

        img.save("output.png");
        img.dispose(); // Free native memory
    }
}
```

---

## Table of Contents

- [Key Features](#key-features)
- [Performance](#performance)
- [Installation](#installation)
- [Try the Demo](#try-the-demo)
- [API Reference](#api-reference)
- [Platform Support](#platform-support)
- [Building from Source](#building-from-source)
- [License](#license)
- [Related Projects](#related-projects)

---

## Key Features

- **ðŸš€ SIMD Acceleration**  Hand-optimized C++ kernels using **AVX2** and **SSE4.1** vector instructions.
- **ðŸš€ Zero-GC Overhead**  Pixels are stored in **off-heap** memory, preventing GC pauses during heavy manipulation.
- **ðŸš€ Advanced Blur Suite**  Real-time Gaussian, Stack (iOS-style), and Kawase blurs.
- **ðŸš€? Fail-Safe JNI**  Robust error handling with `FastImageException` and native handle validation.
- **ðŸš€ Fast Conversion**  Optimized bit-copying between `BufferedImage` and native memory.

---

## Performance

FastImage utilizes the full power of your CPU, outperforming standard Java2D loops by orders of magnitude:

| Operation               | Java2D (BufferedImage) | FastImage (SIMD) | Speedup  |
|:------------------------|:-----------------------|:-----------------|:---------|
| **Brightness**          | ~48.6 ms/op            | **~1.5 ms/op**   | **32x**  |
| **Gaussian Blur (r10)** | ~1100.0 ms/op          | **~170.4 ms/op** | **6.5x** |
| **Grayscale**           | ~20.0 ms/op            | **~1.3 ms/op**   | **15x**  |

*Tested on: 1920x1080 (1080p) ARGB Image on Intel i7-12700K.*

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<!-- FastImage Library -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastimage</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- FastCore (Required Native Loader) -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastcore</artifactId>
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
    implementation 'com.github.andrestubbe:fastimage:0.1.0'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. ðŸš€ **[fastimage-0.1.0.jar](https://github.com/andrestubbe/FastImage/releases/download/0.1.0/fastimage-0.1.0.jar)
   ** (The Core Library)
2. ðŸš€ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (
   The Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.

## Try the Demo

1. Clone this repository: `git clone https://github.com/andrestubbe/FastImage.git`
2. Run the automated showcase: `.\run-demo.bat`

*Includes the interactive Visual Editor and the Blur Gallery.*

---

## API Reference

| Method                     | Description                                              |
|:---------------------------|:---------------------------------------------------------|
| `void grayscale()`         | Converts image to luminance-weighted grayscale via SIMD. |
| `void adjustBrightness(f)` | Scales RGB values with saturation clamping.              |
| `void adjustContrast(f)`   | Adjusts image contrast around the midpoint.              |
| `void blurGaussian(r)`     | High-quality Gaussian blur approximation ($O(N)$).       |
| `void blurStack(r)`        | Extremely fast separable weighted blur.                  |
| `void resize(w, h)`        | Bilinear resizing using native kernels.                  |

---

## Documentation

* **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
* **[REFERENCE.md](docs/REFERENCE.md)**: Full API descriptions, border configurations, and codepoint index.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The engineering rationale for zero-allocation performance.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and planned features.

---

## Platform Support

| Architecture | Instruction Set             | OS            |
|:-------------|:----------------------------|:--------------|
| x64          | **AVX2** (Runtime Dispatch) | Windows 10/11 |
| x64          | **SSE4.1** (Fallback)       | Windows 10/11 |

| Platform      | Status            |
|---------------|-------------------|
| Windows 10/11 | ? Fully Supported |
| Linux         | ðŸš€ Planned        |
| macOS         | ðŸš€ Planned        |

---

## License

MIT License  See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore)  Native Library Loader
- [FastTheme](https://github.com/andrestubbe/FastTheme)  Native Window Styling
- [FastGraphics](https://github.com/andrestubbe/FastGraphics)  Hardware-accelerated 2D Rendering

---

**Part of the FastJava Ecosystem**  *Making the JVM faster. Small package. Maximum speed. Zero bloat. ðŸš€ðŸš€*

