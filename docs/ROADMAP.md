# FastImage Roadmap

- [x] **v0.1.0** — Initial release with off-heap C++ image processing and Kawase blur.
- [x] **v0.1.1** — Integrated `FastSIMD` AVX2 256-bit vector acceleration engine.
- [x] **v0.1.2** — Zero-Copy Pointer/ByteBuffer wrapping & AVX2 Area-Average Anti-Aliasing.
- [x] **v0.1.3** — Catmull-Rom Bicubic Resampling kernel for sharp anti-aliasing.
- [x] **v0.1.4** — Nearest-Neighbor resampling (`resizeNearest`) and OpenMP multi-threading acceleration.
- [ ] **v0.2.0** — FastGPU Vulkan compute shader acceleration for 4K real-time blur.
- [ ] **v0.3.0** — Native PNG/JPEG WebP encoder/decoder integration.
