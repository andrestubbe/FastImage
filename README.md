# FastImage — High-Performance Image Processing for Java

> **Off-heap image processing with SIMD acceleration — 10-50× faster than BufferedImage**

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-3.9+-orange.svg)](https://maven.apache.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io)

---

## Quick Start

```java
import fastimage.FastImage;
import java.awt.image.BufferedImage;

// Load image to off-heap memory (outside JVM heap!)
BufferedImage source = loadImage("photo.jpg");
FastImage img = FastImage.fromBufferedImage(source);

// High-performance operations (SIMD accelerated)
img.resize(1920, 1080);
img.blur(5.0f);
img.adjustBrightness(1.2f);
img.adjustContrast(1.1f);

// Get result back
BufferedImage result = img.toBufferedImage();
```

---

## Installation

### JitPack (Recommended - Ready to Use)

Add repository:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Dependency:
```xml
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastimage</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastimage:v1.0.0'
}
```

### Direct Download

Download JAR from [Releases](https://github.com/andrestubbe/FastImage/releases)

**Required:** FastCore is automatically included via Maven. For direct download, get both:
- `fastimage-1.0.0.jar` — Main library
- `fastcore-1.0.0.jar` — [JNI loader](https://github.com/andrestubbe/FastCore/releases)

---

## Project Structure

```
fastimage/
├── .github/workflows/          # CI/CD
├── examples/00-basic-usage/     # Usage demo + benchmark
│   ├── pom.xml
│   └── src/main/java/fastimage/
├── native/
│   ├── FastImage.cpp          # SIMD native implementation
│   ├── FastImage.h              # Header file
│   └── FastImage.def            # JNI exports
├── src/main/java/fastimage/     # Java API
│   └── FastImage.java
├── compile.bat                 # Native build script
├── COMPILE.md                  # Build instructions
├── pom.xml                     # Maven config
└── README.md                   # This file
```

**Optional folders** (add when needed):
- `src/test/java/` - JUnit tests (Maven recognizes automatically)

**Optional markdown docs** (root level, as needed):
- `BENCHMARK.md` - Performance results
- `TODO.md` - Development roadmap
- `DEPLOYMENT.md` - Release guide
- `PROMOTION.md` - Social media content

**Why `examples/` on root level?**
- Not part of the library → separate mini-projects
- Not tests → tutorials for users
- Each example has its own `pom.xml` → runnable standalone
- Copy-paste friendly → users can use as starter template

---

## Features

| Operation | Java BufferedImage | FastImage | Speedup |
|-----------|-------------------|-----------|---------|
| **Blur (4K)** | 850ms | 45ms | **19×** |
| **Resize (Lanczos)** | 600ms | 35ms | **17×** |
| **Grayscale** | 120ms | 8ms | **15×** |
| **Brightness** | 95ms | 6ms | **16×** |
| **Memory (4K)** | 280MB Heap | 42MB Off-Heap | **6.7× less** |

### Supported Operations

- **Geometry:** resize, rotate, flip, crop
- **Filters:** blur (gaussian), sharpen, edge detection
- **Color:** brightness, contrast, saturation, grayscale
- **Effects:** vignette, sepia, threshold
- **I/O:** PNG/JPEG load/save (parallel encoding)

## Building

See [COMPILE.md](COMPILE.md) for detailed build instructions.

### Quick Build

```bash
# Build native DLL
compile.bat

# Build JAR with DLL
mvn clean package -DskipTests
```

### Run Benchmark

```bash
cd examples/00-basic-usage
mvn compile exec:java
```

Result: Side-by-side comparison BufferedImage vs FastImage

---

## Why FastImage?

**Java BufferedImage Problems:**
- Stores pixels in JVM heap → GC pressure, pauses
- No SIMD optimization → slow pixel loops  
- Format conversions allocate new arrays
- `getRGB()`/`setRGB()` have bounds-check overhead

**FastImage Solutions:**
- **Off-heap storage** — ByteBuffer outside JVM heap
- **SIMD acceleration** — SSE/AVX native operations
- **Zero-copy views** — Crop without copying pixels
- **Batch operations** — Chain filters efficiently

## Benchmark

See `examples/00-basic-usage/` for live benchmark:

```
Benchmark: 4K Image Processing
==============================
Blur 10px:
  BufferedImage:  850ms
  FastImage:       45ms  ← 19× faster

Resize to 1080p:
  BufferedImage:  600ms  
  FastImage:       35ms  ← 17× faster

Memory Usage:
  BufferedImage:  280MB (Heap)
  FastImage:       42MB (Off-Heap)
```

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
