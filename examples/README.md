# 🖼 FastImage Examples

Self-contained example projects demonstrating the power and performance of FastImage.

## 📂 Featured Examples

*   **[BasicUsage](./BasicUsage)**: Minimal integration example. Start here to learn the core API.
*   **[ResizeDemo](./ResizeDemo)**: Shows high-quality bilinear and bicubic resizing performance.
*   **[BlurGallery](./BlurGallery)**: A visual comparison of all blur algorithms (Gaussian, Stack, Kawase).
*   **[FilterChain](./FilterChain)**: Demonstrates how to chain multiple native operations without returning to the JVM heap.
*   **[VisualEditor](./VisualEditor)**: **Showcase Demo** - A real-time image editor showing SIMD speed in action.
*   **[Benchmark](./Benchmark)**: Side-by-side performance comparison with Java2D.

---

## 🚀 Running Examples

Each example is a standalone Maven project. To run the Visual Editor (Showcase):

```powershell
cd examples/VisualEditor
mvn compile exec:java
```

> [!NOTE]
> Ensure you have run `mvn install` in the root directory first so the examples can find the `fastimage` dependency.
