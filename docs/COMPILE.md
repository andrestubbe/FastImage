# Building FastImage 🛠️

Complete build guide for compiling the native C++ AVX2 SIMD image processing engine and packaging the Java JAR.

---

## Prerequisites

* **Windows 10 or 11 (64-bit)**
* **JDK 17+** ([Eclipse Adoptium](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/))
* **Visual Studio 2022 or 2026** (Community, Professional, or Enterprise) with "Desktop development with C++" workload
* **Windows 10/11 SDK** (installed with Visual Studio)
* **Maven 3.9+**

---

## Automated One-Click Build

FastImage provides an automated build script with Visual Studio and JDK discovery:

```cmd
# In the FastImage repository root:
compile.bat
```

What `compile.bat` does automatically:
1. Detects Visual Studio 2026 / 2022 Community via `vswhere.exe`.
2. Sets up the 64-bit developer environment (`vcvars64.bat`).
3. Compiles `src/main/native/fastimage.cpp` with MSVC AVX2 flags:
   - `/O2` (maximum speed)
   - `/arch:AVX2` (256-bit SIMD vectorization)
   - `/D_CRT_SECURE_NO_WARNINGS`
4. Automatically copies `fastimage.dll` to:
   - `src/main/resources/native/fastimage.dll`
   - `src/main/resources/win32-x86-64/fastimage.dll`
   - `%USERPROFILE%\.fastcore\native\fastimage\fastimage.dll`

---

## Maven Java Packaging

Once the native DLL is compiled, build and install to your local Maven repository:

```bash
# Build and install to ~/.m2/repository
mvn clean install -DskipTests
```

---

## JMH Benchmarking

To build and execute the official JMH benchmark suite:

```cmd
run-benchmark.bat
```

---
**Part of the FastJava Ecosystem** — *Making the JVM faster. ⚡*
