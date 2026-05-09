# FastImage Examples & Demos

This directory contains several projects demonstrating the performance and features of the FastImage library.

## Core Demos

-   **[Visual Editor](./VisualEditor)**: Interactive real-time editor showing live previews of filters (Brightness, Contrast, Blur, Grayscale).
-   **[Blur Gallery](./BlurGallery)**: Asynchronous performance comparison between Java2D and SIMD-accelerated blurs.
-   **[Resize Demo](./ResizeDemo)**: Shows the difference between various interpolation methods (Bilinear vs Bicubic).
-   **[Basic Usage](./BasicUsage)**: Minimal boilerplate example for integration.

## Bing Showcase Demos

-   **[Resize Benchmark](./ResizeBenchmark_Bing)**: Stress-test scaling 4K images to 1080p. Shows parallel progress bars and Ops/s.
-   **[Pipeline Demo](./PipelineDemo_Bing)**: Visualizes a multi-stage chain (Blur -> Contrast -> Grayscale) with per-step timing.
-   **[Batch Processing](./BatchProcessing_Bing)**: Demonstrates off-heap efficiency by processing 100 images in parallel.

## Running the Demos

You can run all demos using the root launcher:
```powershell
.\run-demo.bat
```
