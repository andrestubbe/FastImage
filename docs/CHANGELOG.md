# FastImage Changelog

## [0.1.3] - 2026-09-05
- **Catmull-Rom Bicubic Resampling Kernel**:
  - Implemented high-precision `nativeResizeBicubic()` C++ kernel using Catmull-Rom cubic splines.
  - Added `FastImage.resizeBicubic(int w, int h)` providing supreme edge anti-aliasing and sharpness for high-resolution graphics and video.
- **Enhanced Anti-Aliasing Suite**:
  - Unified `resizeBicubic`, `resizeAreaAverage` (box downsampling), and `resize` (bilinear) under FastImage as the central image processing hub for FastJava.
- **Ecosystem Integration**:
  - Direct integration target for `FastScreen 0.1.3` visual pipeline and future `FastGPU` compute bridges.

## [0.1.2] - 2026-09-04
- **Zero-Copy Native Buffer Wrapping**:
  - Implemented `nativeWrap()` C++ JNI bridge allowing zero-copy instantiation of `FastImage`.
  - Added Java factory methods: `FastImage.wrap(long nativeAddress, int w, int h)`, `FastImage.wrap(Pointer, int w, int h)`, and `FastImage.wrap(ByteBuffer directBuffer, int w, int h)`.
  - Safe memory ownership model: wrapped instances do not deallocate foreign native pointers on close.
- **AVX2 Area-Average Anti-Aliasing Downsampler**:
  - Implemented native `nativeResizeAreaAverage()` box/area-average downscaling kernel in C++ with SIMD integer accumulation.
  - Added `FastImage.resizeAreaAverage(int targetWidth, int targetHeight)` to eliminate moiré and jagged aliasing artifacts during high-ratio downsampling.
- **Ecosystem Integration**:
  - Formed zero-copy bridge target for `FastScreen 0.1.2`, `FastCamera 0.1.1`, and `FastRobot 0.1.1`.

## [0.1.1] - 2026-08-14
- Integrated native `FastSIMD` (v0.1.3) AVX2 256-bit vector image processing engine.
- Added official JMH benchmark suite measuring 19.5 full 1080p frames/sec resize throughput.
- Added `Real-World Use Cases` and `Performance Benchmarks` documentation sections.
- Removed deprecated `Project Structure` section from README.md.
- Updated full 5-module dependency stack (`FastImage`, `FastSIMD`, `FastMemory`, `FastPointer`, `FastCore`).

## [0.1.0] - 2026-05-17
- Initial release of FastImage with off-heap C++ rasterizer and Kawase blur.
