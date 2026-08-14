# FastImage Changelog

## [0.1.1] - 2026-08-14
- Integrated native `FastSIMD` (v0.1.3) AVX2 256-bit vector image processing engine.
- Added official JMH benchmark suite measuring 19.5 full 1080p frames/sec resize throughput.
- Added `Real-World Use Cases` and `Performance Benchmarks` documentation sections.
- Removed deprecated `Project Structure` section from README.md.
- Updated full 5-module dependency stack (`FastImage`, `FastSIMD`, `FastMemory`, `FastPointer`, `FastCore`).

## [0.1.0] - 2026-05-17
- Initial release of FastImage with off-heap C++ rasterizer and Kawase blur.
