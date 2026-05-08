# FastImage — Ultra-Fast Native Image Processing for Java [v0.1.0]

**A high-performance image processing library for the FastJava ecosystem. Replaces BufferedImage with SIMD-accelerated native filters.**

[![Status](https://img.shields.io/badge/status-v0.1.0--alpha-orange.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

**FastImage** is built for real-time visual effects. By using native SIMD instructions (AVX2/SSE), it delivers blurs and filters dramatically faster than standard Java `BufferedImageOp`. It stores pixel data in off-heap memory, reducing GC pressure and enabling zero-copy integration with other FastJava modules.

## Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [API Highlights](#api-highlights)
- [License](#license)

## Features
- **🖼️ SIMD Accelerated**: Native Box, Gaussian, Stack, and Kawase Blurs using AVX2/SSE.
- **⚡ Real-Time Smooth**: 10-50x faster than standard Java image processing.
- **📦 Zero-Copy Ready**: Direct memory access via `fromNativeHandle` for integration with modules like `FastThumb`.
- **🚀 Off-Heap Memory**: Pixel data is stored outside the JVM heap, avoiding GC pauses.

## Quick Start

```java
FastImage img = FastImage.fromBufferedImage(myImage);
img.blurGaussian(10.0f);
img.adjustBrightness(1.2f);
BufferedImage result = img.toBufferedImage();
img.dispose();
```

## Installation

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

## API Highlights

### Creation & Disposal
- `FastImage.fromBufferedImage(BufferedImage)`: Create from standard Java image.
- `FastImage.create(int width, int height)`: Create empty off-heap image.
- `FastImage.fromNativeHandle(long handle, int w, int h)`: **(Zero-Copy)** Wrap existing native image pointer.
- `dispose()`: Manually free off-heap memory.

### Operations (SIMD)
- `blurGaussian(float radius)` / `blurStack(float radius)` / `blurKawase(float radius, int passes)`
- `resize(int w, int h)` / `crop(x, y, w, h)`
- `grayscale()` / `adjustBrightness(float)` / `adjustContrast(float)`
- `flipHorizontal()` / `flipVertical()`

---

## License
MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*
