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

## 2. Image Operations & Filters

### `resize`
```java
public FastImage resize(int newWidth, int newHeight)
```
Resizes the image to target dimensions using native C++ AVX2 SIMD bilinear interpolation.

---

### `blurKawase`
```java
public FastImage blurKawase(float radius, int passes)
```
Applies high-speed Dual-Kawase blur filtering (ideal for UI translucent overlays).

---

### `grayscale`
```java
public FastImage grayscale()
```
Converts color image pixels to grayscale using AVX2 SIMD luminance weighting.

---

## 3. Export API

### `toBufferedImage`
```java
public BufferedImage toBufferedImage()
```
Creates a standard Java `BufferedImage` representation from the native pixel memory.
