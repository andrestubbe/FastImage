package fastimage;

import fastcore.FastCore;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * FastImage - High-performance off-heap image processing with SIMD
 * acceleration.
 * 
 * Stores pixel data in native memory (off-heap) outside JVM garbage collection,
 * providing 10-50× faster image operations than BufferedImage via SSE/AVX SIMD.
 * 
 * Supported operations:
 * - resize (bilinear, bicubic)
 * - blur (gaussian)
 * - grayscale
 * - brightness/contrast adjustment
 * - flip horizontal/vertical
 * 
 * Memory: Uses ByteBuffer.allocateDirect() - not counted in JVM heap!
 */
public class FastImage {

    static {
        FastCore.loadLibrary("fastimage");
    }

    // Native handles
    private long nativeHandle; // Pointer to native FastImage struct
    private int width;
    private int height;
    private boolean disposed = false;

    /**
     * Create FastImage from BufferedImage (ARGB format)
     */
    public static FastImage fromBufferedImage(BufferedImage img) {
        if (img == null)
            throw new IllegalArgumentException("Image is null");

        int w = img.getWidth();
        int h = img.getHeight();

        // Convert to ARGB if necessary
        BufferedImage argbImg;
        if (img.getType() != BufferedImage.TYPE_INT_ARGB) {
            argbImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            argbImg.getGraphics().drawImage(img, 0, 0, null);
            argbImg.getGraphics().dispose();
        } else {
            argbImg = img;
        }

        // Get pixel data
        int[] pixels = ((DataBufferInt) argbImg.getRaster().getDataBuffer()).getData();

        // Create native image
        FastImage fastImg = new FastImage();
        fastImg.width = w;
        fastImg.height = h;
        fastImg.nativeHandle = nativeCreate(w, h, pixels);

        return fastImg;
    }

    /**
     * Create empty FastImage with given dimensions
     */
    public static FastImage create(int width, int height) {
        FastImage img = new FastImage();
        img.width = width;
        img.height = height;
        img.nativeHandle = nativeCreateEmpty(width, height);
        return img;
    }

    /**
     * Internal: Create FastImage from an existing native handle.
     * The native handle must point to a valid native FastImage struct.
     */
    public static FastImage fromNativeHandle(long handle, int width, int height) {
        if (handle == 0)
            throw new FastImageException("Native handle is null (0)");
        FastImage img = new FastImage();
        img.width = width;
        img.height = height;
        img.nativeHandle = handle;
        return img;
    }

    private FastImage() {
    }

    // === Core Operations ===

    /**
     * Resize image using bilinear interpolation
     * 
     * @param newWidth  target width
     * @param newHeight target height
     */
    public FastImage resize(int newWidth, int newHeight) {
        checkDisposed();
        if (newWidth <= 0 || newHeight <= 0) {
            throw new FastImageException("Invalid dimensions: " + newWidth + "x" + newHeight);
        }
        nativeResize(nativeHandle, newWidth, newHeight);
        this.width = newWidth;
        this.height = newHeight;
        return this;
    }

    /**
     * Fast box blur - quickest approximation.
     * Good for real-time effects where speed matters.
     * 
     * @param radius blur radius (0-50)
     */
    public FastImage blurBox(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        if (radius == 0) return this;
        nativeBlurBox(nativeHandle, radius);
        return this;
    }

    /**
     * Separable Gaussian blur - high quality, smooth results.
     * Slower than box blur but looks much better.
     * 
     * @param radius blur radius (0-50)
     */
    public FastImage blurGaussian(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        if (radius == 0) return this;
        nativeBlurGaussian(nativeHandle, radius);
        return this;
    }

    /**
     * Stack blur - CSS backdrop-filter quality.
     * Best balance of speed and quality for UI effects.
     * 
     * @param radius blur radius (0-100)
     */
    public FastImage blurStack(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        if (radius == 0) return this;
        nativeBlurStack(nativeHandle, radius);
        return this;
    }

    /**
     * Kawase blur - multi-pass blur used by Apple/Google.
     * Very soft edges with configurable quality.
     * 
     * @param radius blur radius (0-50)
     * @param passes number of passes (1-5, higher = softer)
     */
    public FastImage blurKawase(float radius, int passes) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        if (passes < 1 || passes > 10) throw new FastImageException("Invalid passes: " + passes);
        nativeBlurKawase(nativeHandle, radius, passes);
        return this;
    }

    /**
     * Dual Kawase blur - premium 2-pass algorithm.
     * Best quality with excellent performance.
     * 
     * @param radius blur radius (0-50)
     */
    public FastImage blurDualKawase(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        nativeBlurDualKawase(nativeHandle, radius);
        return this;
    }

    /**
     * Mipmapped blur - for very large blur radii (100+).
     * Uses downscaling + small blur + upscaling.
     * 
     * @param radius blur radius (0-200)
     */
    public FastImage blurMipmapped(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        nativeBlurMipmapped(nativeHandle, radius);
        return this;
    }

    /**
     * Convert to grayscale
     */
    public FastImage grayscale() {
        checkDisposed();
        nativeGrayscale(nativeHandle);
        return this;
    }

    /**
     * Adjust brightness
     * 
     * @param factor 0.0 = black, 1.0 = unchanged, 2.0 = double brightness
     */
    public FastImage adjustBrightness(float factor) {
        checkDisposed();
        if (factor < 0) throw new FastImageException("Factor cannot be negative: " + factor);
        nativeBrightness(nativeHandle, factor);
        return this;
    }

    /**
     * Adjust contrast
     * 
     * @param factor 0.0 = gray, 1.0 = unchanged, 2.0 = double contrast
     */
    public FastImage adjustContrast(float factor) {
        checkDisposed();
        if (factor < 0) throw new FastImageException("Factor cannot be negative: " + factor);
        nativeContrast(nativeHandle, factor);
        return this;
    }

    /**
     * Flip image horizontally
     */
    public FastImage flipHorizontal() {
        checkDisposed();
        nativeFlipH(nativeHandle);
        return this;
    }

    /**
     * Flip image vertically
     */
    public FastImage flipVertical() {
        checkDisposed();
        nativeFlipV(nativeHandle);
        return this;
    }

    /**
     * Crop image to region (creates new FastImage, leaves original)
     * 
     * @param x left coordinate
     * @param y top coordinate
     * @param w width
     * @param h height
     */
    public FastImage crop(int x, int y, int w, int h) {
        checkDisposed();
        if (x < 0 || y < 0 || x + w > width || y + h > height) {
            throw new IllegalArgumentException("Crop region outside image bounds");
        }
        FastImage cropped = new FastImage();
        cropped.width = w;
        cropped.height = h;
        cropped.nativeHandle = nativeCrop(nativeHandle, x, y, w, h);
        return cropped;
    }

    // === Conversion ===

    /**
     * Convert to BufferedImage (TYPE_INT_ARGB)
     */
    public BufferedImage toBufferedImage() {
        checkDisposed();
        int[] pixels = new int[width * height];
        nativeGetPixels(nativeHandle, pixels);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] targetPixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, targetPixels, 0, pixels.length);

        return img;
    }

    /**
     * Get raw pixel array (ARGB format) - creates copy
     */
    public int[] getPixels() {
        checkDisposed();
        int[] pixels = new int[width * height];
        nativeGetPixels(nativeHandle, pixels);
        return pixels;
    }

    // === Getters ===

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Free native memory
     */
    public void dispose() {
        if (!disposed && nativeHandle != 0) {
            nativeDispose(nativeHandle);
            nativeHandle = 0;
            disposed = true;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    private void checkDisposed() {
        if (disposed)
            throw new IllegalStateException("FastImage has been disposed");
    }

    // === Native Methods ===

    private static native long nativeCreate(int width, int height, int[] pixels);

    private static native long nativeCreateEmpty(int width, int height);

    private static native void nativeDispose(long handle);

    private static native void nativeResize(long handle, int newWidth, int newHeight);

    private static native void nativeBlurBox(long handle, float radius);

    private static native void nativeBlurGaussian(long handle, float radius);

    private static native void nativeBlurStack(long handle, float radius);

    private static native void nativeBlurKawase(long handle, float radius, int passes);

    private static native void nativeBlurDualKawase(long handle, float radius);

    private static native void nativeBlurMipmapped(long handle, float radius);

    private static native void nativeGrayscale(long handle);

    private static native void nativeBrightness(long handle, float factor);

    private static native void nativeContrast(long handle, float factor);

    private static native void nativeFlipH(long handle);

    private static native void nativeFlipV(long handle);

    private static native long nativeCrop(long handle, int x, int y, int w, int h);

    private static native void nativeGetPixels(long handle, int[] pixels);
}
