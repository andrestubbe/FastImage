# Changelog

## [0.1.0] - 2026-05-08
### Added
- Native SIMD-accelerated image processing core (SSE4.1/AVX2).
- **Runtime SIMD Dispatching**: Automatic switching between AVX2 and SSE4.1 based on CPU features.
- **Robust Error Handling**: Introduced `FastImageException` and comprehensive JNI handle validation.
- **Unit Testing Suite**: Full JUnit 5 coverage for lifecycle, validation, and pixel-level correctness.
- **High-Performance Blurs**: O(N) Sliding Window Box Blur, Stack Blur (iOS-style), and Gaussian approximation.
- **Advanced Filtering**: Saturated math for Brightness and Contrast adjustments.
- **Optimized Memory**: 32-byte alignment for maximum AVX2 throughput.
- **BluePrint Documentation**: Professional API Design and updated README.
- High-quality Bilinear Resizing.
- Zero-copy native handle integration for the FastJava ecosystem.
