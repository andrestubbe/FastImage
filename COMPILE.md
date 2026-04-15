# Building FastImage from Source

FastImage uses JNI for SIMD-accelerated image processing.

## Prerequisites

- **JDK 17+** — [Download](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
- **Visual Studio 2019+** — Only for modules with native code (JNI)

## Quick Build

```bash
# Clone and build
git clone https://github.com/andrestubbe/FastImage.git
cd FastImage

# Build native DLL first
compile.bat

# Build JAR with DLL
mvn clean package -DskipTests
```

## Build Commands

| Command | Purpose |
|---------|---------|
| `mvn clean compile` | Compile only |
| `mvn clean package` | Build JAR |
| `mvn clean package -DskipTests` | Fast build (no tests) |
| `mvn test` | Run tests |

## Modules with Native Code

If this module uses JNI (C++), build the DLL first:

```bash
# Windows: Build native DLL
compile.bat

# Then build JAR
mvn clean package
```

**`compile.bat` features:**
- Auto-detects Visual Studio (2019/2022, Community/Enterprise/Professional/BuildTools)
- Auto-detects JAVA_HOME (JDK 17/21/25)
- No manual path configuration needed

## Running Examples

All runnable code is in `examples/`:

```bash
cd examples/00-basic-usage
mvn compile exec:java
```

## JNI Exports (.def File)

When using JNI, you MUST export your native functions in a `.def` file:

```
native/
├── FastImage.def          # JNI exports (required!)
├── FastImage.cpp          # SIMD implementation
└── FastImage.h            # Header file
```

**FastImage.def format:**
```
LIBRARY fastimage
EXPORTS
    Java_fastimage_FastImage_nativeCreate
    Java_fastimage_FastImage_nativeBlur
    Java_fastimage_FastImage_nativeResize
    ...
```

**Important:** Function names must match Java's expected format:
- Pattern: `Java_packagename_Classname_methodname`
- Example: `Java_fastrobot_FastRobot_captureScreen`

Without the `.def` file, JNI methods won't be exported and you'll get `UnsatisfiedLinkError`.

## Troubleshooting

**"Cannot find DLL"** — Run `compile.bat` first (Windows)

**"UnsatisfiedLinkError"** — Common causes:
1. DLL not in JAR — Check `pom.xml` resources section
2. JNI exports missing — Verify `.def` file and `/DEF:` flag in compile.bat
3. Wrong function name — Must match `Java_package_Class_method` pattern
4. Wrong architecture — Ensure JDK and DLL match (x64 vs x86)

**"Java version mismatch"** — Ensure JDK 17+ is installed and JAVA_HOME is set

**Debugging JNI:**
```cpp
// Add debug output to native code
printf("[DEBUG] Method called\n");
fflush(stdout);  // Ensure output appears immediately
```

**Check DLL exports:**
```bash
dumpbin /exports build\fastimage.dll
```

## Running Benchmark

```bash
cd examples/00-basic-usage
mvn compile exec:java
```

This runs a side-by-side benchmark comparing BufferedImage vs FastImage performance.
