# FastImage API Reference Manual

`FastImage` provides high-performance off-heap image processing for Java with native AVX2 SIMD bilinear scaling, Dual-Kawase blur filters, and color transformations.

---

## 1. Creation & Factory API

### `create`
```java
public static FastImage create(int width, int height)
```
Allocates an unmanaged off-heap 32-bit ARGB pixel buffer in native memory.

---

### `fromBufferedImage`
```java
public static FastImage fromBufferedImage(BufferedImage img)
```
Converts a Java `BufferedImage` into an off-heap `FastImage` instance.

---

### `wrap`
```java
public static FastImage wrap(long nativeAddress, int width, int height)
public static FastImage wrap(Pointer pointer, int width, int height)
public static FastImage wrap(ByteBuffer directBuffer, int width, int height)
```
Zero-copy wraps an existing off-heap native memory address, FastPointer, or Direct `ByteBuffer` without copying bytes. The wrapped instance does not deallocate foreign memory when closed.

---

## 2. Image Operations & Filters

### `resize`
```java
public FastImage resize(int newWidth, int newHeight)
```
Resizes the image to target dimensions using native C++ AVX2 SIMD bilinear interpolation.

---

### `resizeBicubic`
```java
public FastImage resizeBicubic(int newWidth, int newHeight)
```
Ultra-sharp Catmull-Rom Bicubic spline resampling. Delivers maximum visual fidelity and edge anti-aliasing when scaling graphics or screen captures.

---

### `resizeNearest`
```java
public FastImage resizeNearest(int newWidth, int newHeight)
```
Ultra-fast Nearest-Neighbor (point sampling) scaling kernel. Ideal for pixel art, retro rendering, or maximum framerate downsampling.

---

### `resizeAreaAverage`
```java
public FastImage resizeAreaAverage(int targetWidth, int targetHeight)
```
High-quality Anti-Aliasing downsampler using OpenMP multi-threaded box area averaging. Eliminates jagged edges and moiré patterns when scaling down desktop or camera feeds.

---

### `crop`
```java
public FastImage crop(int x, int y, int width, int height)
```
Extracts a sub-region into a new `FastImage` instance.

---

### `flipHorizontal` / `flipVertical`
```java
public FastImage flipHorizontal()
public FastImage flipVertical()
```
Flips the image buffer along the horizontal or vertical axis in-place.

---

### `blurKawase`
```java
public FastImage blurKawase(float radius, int passes)
```
Applies multi-pass Kawase blur filtering (ideal for UI translucent overlays).

---

### `blurDualKawase`
```java
public FastImage blurDualKawase(float radius)
```
Applies high-speed 2-pass Dual-Kawase downsampling and upsampling blur.

---

### `blurGaussian`
```java
public FastImage blurGaussian(float radius)
```
Applies smooth separable Gaussian blur kernel.

---

### `blurStack`
```java
public FastImage blurStack(float radius)
```
Applies CSS `backdrop-filter` grade fast stack blur.

---

### `blurBox`
```java
public FastImage blurBox(float radius)
```
Applies fast box blur.

---

### `blurMipmapped`
```java
public FastImage blurMipmapped(float radius)
```
Applies hierarchical down/upsample blur for very large radii (100+ pixels).

---

### `grayscale`
```java
public FastImage grayscale()
```
Converts color image pixels to grayscale using AVX2 SIMD luminance weighting.

---

### `adjustBrightness`
```java
public FastImage adjustBrightness(float factor)
```
Scales luminance of all pixels (`1.0` = original brightness).

---

### `adjustContrast`
```java
public FastImage adjustContrast(float factor)
```
Adjusts image contrast ratio centered around mid-gray (`128`).

---

## 3. Export & Interop API

### `toBufferedImage`
```java
public BufferedImage toBufferedImage()
```
Creates a standard Java `BufferedImage` representation from the native pixel memory.

---

### `getPixels`
```java
public void getPixels(int[] destinationBuffer)
```
Direct zero-allocation copy into a caller-supplied pre-allocated `int[]` array.

---

### `getDirectBuffer`
```java
public ByteBuffer getDirectBuffer()
```
Exposes the native memory address as a direct `java.nio.ByteBuffer` with zero copies.

---

### `getPointer`
```java
public Pointer getPointer()
```
Returns a `fastpointer.Pointer` wrapper around the native physical memory address.
