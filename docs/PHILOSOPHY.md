# The Philosophy of FastImage 💡

> [!IMPORTANT]
> **"Keine Kopien. Niemals. Kritischer JNI-Pfad. Native-First Performance."**

FastImage is built on the conviction that 2D image processing, UI blurring, and computer vision pipelines in Java must never be crippled by JVM heap allocations, garbage collection stalls, or slow scalar rasterizers.

## Core Tenets

1.  **Native-First SIMD Execution**
    Bypass standard Java2D and `BufferedImage` software loops to reach physical CPU limits using hand-tuned C++ with 256-bit AVX2 vector intrinsics.

2.  **Off-Heap Zero-GC Architecture**
    Maintain pixel buffers strictly in native unmanaged memory. Zero JVM Garbage Collection pauses even when processing uncompressed 1080p and 4K frames at 60+ FPS.

3.  **Zero-Copy Foreign Buffer Interop**
    Seamlessly wrap raw 64-bit pointers, `FastPointer` handles, and `DirectByteBuffer` allocations from sibling modules (`FastScreen`, `FastCamera`, `FastRobot`) without duplicating memory.

4.  **Hardware-Grade Filtering Algorithms**
    Provide modern GPU-grade blur (Dual-Kawase) and box area-average downsampling in native CPU kernels for high-quality UI overlays and downscaling without aliasing artifacts.

5.  **FastJava Blueprint Consistency**
    As part of the **FastJava** ecosystem:
    *   **Native Backend**: Direct C++ implementation with AVX2 optimizations.
    *   **Unified Loading**: Powered by `FastCore` for zero-dependency native deployment.
    *   **Production Quality**: High-performance, clean chaining API, and thorough JMH profiling.

---
**⚡ FastImage — Powering the next generation of Native Java.**
