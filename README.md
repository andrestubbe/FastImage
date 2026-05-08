# FastImage — SIMD‑Accelerated, Off‑Heap Image Processing for Java

[![FastJava](https://img.shields.io/badge/Ecosystem-FastJava-0078D4.svg?style=for-the-badge&logo=java)](https://github.com/andrestubbe)
[![Release](https://img.shields.io/badge/Release-v0.1.0--STABLE-green.svg?style=for-the-badge)](https://github.com/andrestubbe/FastImage/releases)
[![Performance](https://img.shields.io/badge/Performance-10--50x_Faster-blue.svg?style=for-the-badge)](https://github.com/andrestubbe/FastImage#benchmarks)

**FastImage** ist eine ultra‑schnelle, native‑beschleunigte Image‑Processing‑Engine für Java, gebaut für das FastJava‑Ecosystem. Es kombiniert AVX/SSE SIMD, off‑heap Storage, zero‑copy Pipelines und eine fluent API, um typische BufferedImage‑Operationen **10–50× schneller** auszuführen — ohne GC‑Pressure, ohne Pixel‑Loops, ohne JVM‑Overhead.

### Highlights
- ⚡ **SIMD Accelerated**: AVX2 & SSE4.1 optimierte Kernel für maximale CPU-Ausnutzung.
- 📦 **Off-Heap Memory**: Pixel werden außerhalb des Java-Heaps gespeichert (kein GC-Overhead).
- 🧬 **Fluent API**: Intuitive Verkettung von Operationen (`resize().blur().grayscale()`).
- 🛡️ **Fail-Safe JNI**: Robuste Fehlerbehandlung und Bounds-Checks.

---

### Tags
`java` `image-processing` `simd` `avx` `sse` `native` `jni` `off-heap` `high-performance` `fastjava` `graphics` `zero-copy`

---

## 📖 Table of Contents
- [Key Features](#key-features)
- [Performance](#performance)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Demos](#demos)
- [API Reference](#api-reference)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)

---

## 🚀 Key Features

- **⚡ SIMD Acceleration** — Hand-optimized C++ kernels using **AVX2** and **SSE4.1** vector instructions.
- **🧠 Zero-GC Overhead** — Pixels are stored in **off-heap** memory, preventing GC pauses during heavy manipulation.
- **🌀 Advanced Blur Suite** — Real-time Gaussian, Stack (iOS-style), and Kawase blurs with $O(N)$ complexity.
- **📦 Ecosystem Ready** — Native handle hand-off from **FastThumb** and **FastGraphics**.
- **🔄 Fast Conversion** — Optimized bit-copying between `BufferedImage` and native memory.

---

## 📊 Performance

*Tested on: 1920x1080 (1080p) ARGB Image*

| Operation | Java2D (BufferedImage) | FastImage (SIMD) | Speedup |
| :--- | :--- | :--- | :--- |
| **Brightness** | ~48.6 ms/op | **~1.5 ms/op** | **32x** |
| **Gaussian Blur (r10)** | ~1100.0 ms/op | **~170.4 ms/op** | **6.5x** |
| **Grayscale** | ~20.0 ms/op | **~1.3 ms/op** | **15x** |

---

## 🛠 Quick Start

```java
import fastimage.FastImage;
import java.awt.image.BufferedImage;

public class Demo {
    public static void main(String[] args) {
        // Wrap an existing image (copies data to native memory)
        FastImage img = FastImage.fromBufferedImage(myPhoto);
        
        // Apply high-performance filters
        img.adjustContrast(1.2f);
        img.blurGaussian(15.0f);
        img.grayscale();
        
        // Export back to Java UI
        BufferedImage result = img.toBufferedImage();
        
        // CRITICAL: Free native memory when done
        img.dispose();
    }
}
```

---

## 📦 Installation

FastImage requires **FastCore** for native library management.

### Maven (JitPack)
```xml
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastimage</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---

## 🖥 Try the Demos

We provide several standalone demos to showcase the performance:

1.  **[Visual Editor](./examples/VisualEditor)** — Interactive split-screen editor (Real-time).
2.  **[Blur Gallery](./examples/BlurGallery)** — Comparison of different blur algorithms.
3.  **[Benchmark](./examples/Benchmark)** — Run the performance tests on your own machine.

To run the main showcase:
```powershell
.\run-demo.bat
```

---

## 📖 API Reference

| Method | Description |
| :--- | :--- |
| `void grayscale()` | Converts image to luminance-weighted grayscale via SSE4.1. |
| `void adjustBrightness(float f)` | Scales RGB values. Supports factors > 1.0. |
| `void adjustContrast(float f)` | Adjusts image contrast around the 128-midpoint. |
| `void blurGaussian(float r)` | High-quality Gaussian blur approximation ($O(N)$). |
| `void blurStack(float r)` | iOS-style soft blur, extremely fast. |
| `void resize(int w, int h)` | Bilinear/Bicubic resizing using native kernels. |

---

## 💻 Platform Support

| Architecture | Instruction Set | OS |
| :--- | :--- | :--- |
| x64 | **AVX2** (Recommended) | Windows 10/11 |
| x64 | **SSE4.1** (Fallback) | Windows 10/11 |

---

## 🗺 Related Projects

- [FastThumb](https://github.com/andrestubbe/FastThumb) — Native Shell Thumbnails.
- [FastGraphics](https://github.com/andrestubbe/FastGraphics) — DirectX 12 Rendering Engine.
- [FastCore](https://github.com/andrestubbe/FastCore) — Native Library Infrastructure.

---

**Made with ⚡ by Andre Stubbe**

<!-- 
SEO Keywords: java, jni, simd, avx2, sse4, image processing, blur, gaussian, fastjava
-->
