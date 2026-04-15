# FastImage — High-Performance Image Processing for Java

> **SIMD-accelerated, off-heap image processing — 10-50× faster than BufferedImage**
> 
> Native speed for Java: Resize, blur, grayscale, brightness with zero GC pressure

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-3.9+-orange.svg)](https://maven.apache.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io)

---

## 🚀 Quick Start

```java
import fastimage.FastImage;

// Chain operations fluently - all SIMD accelerated
FastImage img = FastImage.fromBufferedImage(source)
    .resize(1920, 1080)
    .blur(5.0f)
    .grayscale()
    .adjustBrightness(1.2f);

BufferedImage result = img.toBufferedImage();
img.dispose();  // Free native memory
```

---

## 📦 Installation

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastimage</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Gradle

```groovy
repositories { maven { url 'https://jitpack.io' } }
dependencies { implementation 'com.github.andrestubbe:fastimage:v1.0.0' }
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

**Why `examples/` on root level?**
- Not part of the library → separate mini-projects
- Not tests → tutorials for users
- Each example has its own `pom.xml` → runnable standalone
- Copy-paste friendly → users can use as starter template

---

## ⚡ Performance Benchmarks

| Operation | BufferedImage | FastImage | Speedup | Memory |
|-----------|--------------|-----------|---------|--------|
| **Blur 4K** | 850ms | 45ms | **19×** | 6.7× less |
| **Resize 4K→1080p** | 600ms | 35ms | **17×** | Off-heap |
| **Grayscale** | 120ms | 8ms | **15×** | No GC pressure |
| **Brightness** | 95ms | 6ms | **16×** | Direct access |
| **Full Pipeline** | 2.5s | 120ms | **21×** | Zero-copy |

*Hardware: i7-12700H, Windows 11, Java 17. Measured with included benchmark.*

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

## 🧠 Why FastImage?

**BufferedImage Problems:**
- ❌ JVM heap storage → GC pauses with large images
- ❌ No SIMD → pixel-by-pixel Java loops
- ❌ `getRGB()`/`setRGB()` → bounds checks, slow

**FastImage Solutions:**
- ✅ **Off-heap** — ByteBuffer outside JVM heap, no GC
- ✅ **SIMD** — SSE2/AVX native operations
- ✅ **Zero-copy** — Crop without copying pixels
- ✅ **Fluent API** — Chain operations efficiently

## 🗺 Part of FastJava Ecosystem

| Module | Purpose | Link |
|--------|---------|------|
| **FastCore** | JNI loader | [GitHub](https://github.com/andrestubbe/FastCore) |
| **FastGraphics** | GPU rendering | [GitHub](https://github.com/andrestubbe/FastGraphics) |
| **FastRobot** | Screen capture | [GitHub](https://github.com/andrestubbe/FastRobot) |
| **FastMath** | SIMD math | [GitHub](https://github.com/andrestubbe/FastMath) |

## 📚 Examples

Every feature has a standalone example in `examples/`:

```bash
cd examples/00-basic-usage
mvn compile exec:java    # Run benchmark
```

| Example | Demonstrates |
|---------|-------------|
| `00-basic-usage` | Benchmark + chain API |
| `01-resize` | Resize algorithms (coming) |
| `02-blur` | Blur with slider (coming) |
| `03-visual-effects` | Split-screen demo (coming) |

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
