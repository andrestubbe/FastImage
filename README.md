# ⚡ FastImage v0.1.0

[![Blueprint](https://img.shields.io/badge/Standard-BluePrint-blue.svg)](https://github.com/andrestubbe/FastJava)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20x64-lightgrey.svg)](#)

**FastImage** is the high-performance imaging core of the **FastJava** ecosystem. It provides ultra-fast, SIMD-accelerated image processing by keeping pixel data in **native memory (off-heap)**, bypassing the JVM's Garbage Collector and utilizing AVX2/SSE4.1 instructions for maximum throughput.

> [!IMPORTANT]
> FastImage is designed for performance-critical UI effects and real-time image processing. It achieves **10-50x speedups** compared to standard `BufferedImage` operations.

---

## 🚀 Key Features

*   **⚡ Native SIMD Acceleration**: Hand-optimized C++ kernels using AVX2 and SSE4.1.
*   **📦 Zero-Copy Integration**: Seamlessly hand over native memory handles from modules like **FastThumb** or **FastGraphics**.
*   **🧠 Off-Heap Storage**: Pixels are stored outside the JVM heap, preventing GC pauses during heavy image manipulation.
*   **🌈 Professional Blur Suite**: Includes Box, Gaussian (O(N)), Stack, Kawase, and Dual-Kawase blurs.
*   **🎨 Advanced Adjustments**: Real-time Brightness, Contrast, Grayscale (Luminance-weighted), and Resizing.
*   **🔄 Instant Conversion**: Efficient bridge to and from `java.awt.image.BufferedImage`.

---

## 📊 Performance Benchmark

*Tested on: 1920x1080 (1080p) Image, 100 Iterations*

| Operation | Java2D (BufferedImage) | FastImage (SIMD) | Speedup |
| :--- | :--- | :--- | :--- |
| **Brightness** | ~48.6 ms/op | **~1.5 ms/op** | **~32x** |
| **Gaussian Blur (r10)** | ~1100.0 ms/op | **~170.4 ms/op** | **~6.5x** |
| **Grayscale** | ~20.0 ms/op | **~1.3 ms/op** | **~15x** |

---

## 🛠 Usage

### Basic Initialization
```java
// From BufferedImage
FastImage img = FastImage.fromBufferedImage(myBuffer);

// Apply real-time effects
img.adjustBrightness(1.2f);
img.blurGaussian(10.0f);
img.grayscale();

// Back to Java2D
BufferedImage result = img.toBufferedImage();
img.dispose(); // Always free native memory!
```

### Zero-Copy from FastThumb
```java
// FastThumb returns a native handle, FastImage wraps it instantly
FastImage thumb = FastThumb.get(path, 256);
thumb.blurStack(5.0f); // Fast blur on the native buffer
```

---

## 🏗 Build Requirements

*   **JDK 17+** (Recommended: JDK 21+)
*   **Visual Studio 2022** (with C++ Desktop workload)
*   **Maven 3.8+**
*   **Architecture**: x64 with AVX2/SSE4.1 support

---

## 🗺 Roadmap

- [x] SIMD-accelerated Brightness/Contrast
- [x] O(N) Sliding Window Box/Gaussian Blur
- [ ] AVX-accelerated Bilinear Resizer
- [ ] Hardware-accelerated GPU fallback via FastGraphics
- [ ] Neural Denoising Filter

---

© 2026 Andre Stubbe - Part of the **FastJava** Blueprint.
