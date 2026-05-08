# FastImage API Design & Architecture

FastImage is designed for ultra-high-performance image processing in Java, bypassing the limitations of the JVM heap and garbage collector.

## Core Philosophy

1.  **Off-Heap Storage**: Pixel data is stored in native memory via `_aligned_malloc`. This avoids GC pressure and allows direct access by native SIMD kernels.
2.  **SIMD Acceleration**: Every core operation is vectorized using Intel SSE4.1 and AVX2 instructions.
3.  **Runtime Dispatching**: The library detects CPU features at startup and automatically chooses the fastest available code path (AVX2 -> SSE4.1 -> Plain C++).
4.  **Zero-Stutter JNI**: JNI transitions are minimized, and long-running operations are designed to be thread-safe for background execution.

## Memory Model

-   **Manual Management**: Since memory is off-heap, `dispose()` must be called to free native resources. A fallback `finalize()` is provided but should not be relied upon.
-   **Alignment**: All buffers are 32-byte aligned to satisfy the requirements of 256-bit AVX instructions.

## Error Handling

-   **Bounds Validation**: All parameters (radii, dimensions, factors) are validated in Java before reaching native code.
-   **Native Safety**: JNI entry points verify native handles. Invalid access throws `FastImageException` instead of crashing the JVM.

## Integration with FastJava

FastImage follows the **BluePrint** standard:
-   Modular architecture.
-   Fluent API for filter chaining.
-   Native performance with Java simplicity.
