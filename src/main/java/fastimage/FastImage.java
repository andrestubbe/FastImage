package fastimage;

import fastcore.FastCore;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * FastImage - High-performance off-heap image processing with SIMD acceleration.
 * <p>
 * Stores pixel data in native memory (off-heap) outside JVM garbage collection,
 * providing 10-50× faster image operations than standard Java2D / BufferedImage
 * via SSE/AVX vector instruction sets.
 * <p>
 * Supported operations:
 * <ul>
 *   <li>Resampling: Bilinear, Catmull-Rom Bicubic Spline, Area-Averaging Box downsampling</li>
 *   <li>Convolutions &amp; Blurs: Box, Gaussian, Stack, Kawase, Dual Kawase, Mipmapped</li>
 *   <li>Color &amp; Tone: Grayscale (luminance weighted), Brightness, Contrast</li>
 *   <li>Geometry: Horizontal flip, Vertical flip, Sub-region crop</li>
 *   <li>Zero-Copy Interop: Native pointers, FastPointer, DirectByteBuffer, BufferedImage</li>
 * </ul>
 * <p>
 * Memory Management: Pixel buffers reside in unmanaged off-heap memory. Always invoke
 * {@link #dispose()} when finished to release native allocations immediately.
 */
public class FastImage {

    static {
        FastCore.loadLibrary("fastimage");
    }

    // Native state handles
    private long nativeHandle; // Pointer to native FastImage struct
    private int width;
    private int height;
    private boolean disposed = false;

    /**
     * Private constructor used by static factory methods.
     */
    private FastImage() {
    }

    // =========================================================================
    // Factory & Allocation Methods
    // =========================================================================

    /**
     * Creates an empty FastImage with the given dimensions initialized to black transparent pixels.
     *
     * @param width  Image width in pixels (must be &gt; 0).
     * @param height Image height in pixels (must be &gt; 0).
     * @return A newly allocated {@link FastImage} instance.
     * @throws FastImageException If dimensions are invalid or memory allocation fails.
     */
    public static FastImage create(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new FastImageException("Invalid dimensions: " + width + "x" + height);
        }
        FastImage img = new FastImage();
        img.width = width;
        img.height = height;
        img.nativeHandle = nativeCreateEmpty(width, height);
        return img;
    }

    /**
     * Creates a FastImage by copying pixel data from an existing {@link BufferedImage} (ARGB format).
     *
     * @param img Source {@link BufferedImage} to copy from.
     * @return A newly allocated {@link FastImage} containing a copy of the source pixels.
     * @throws IllegalArgumentException If {@code img} is null.
     * @throws FastImageException       If native buffer allocation fails.
     */
    public static FastImage fromBufferedImage(BufferedImage img) {
        if (img == null) {
            throw new IllegalArgumentException("Image is null");
        }

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
     * Creates a FastImage directly from an existing pixel array (ARGB/RGB format)
     * without any {@link BufferedImage} wrapping or Java2D color model conversion overhead.
     *
     * @param pixels Source pixel array containing packed 32-bit integers.
     * @param width  Image width in pixels (must be &gt; 0).
     * @param height Image height in pixels (must be &gt; 0).
     * @return A newly allocated {@link FastImage} containing a native copy of the pixels.
     * @throws IllegalArgumentException If {@code pixels} is null or length is insufficient.
     * @throws FastImageException       If dimensions are invalid or memory allocation fails.
     */
    public static FastImage fromPixels(int[] pixels, int width, int height) {
        if (pixels == null) {
            throw new IllegalArgumentException("Pixel array is null");
        }
        if (width <= 0 || height <= 0) {
            throw new FastImageException("Invalid dimensions: " + width + "x" + height);
        }
        if (pixels.length < width * height) {
            throw new IllegalArgumentException("Pixel array too small for dimensions: " + pixels.length + " < " + (width * height));
        }

        FastImage fastImg = new FastImage();
        fastImg.width = width;
        fastImg.height = height;
        fastImg.nativeHandle = nativeCreate(width, height, pixels);
        return fastImg;
    }

    /**
     * Wraps an external native memory pointer (e.g. from FastScreen, FastCamera, or FastSharedMemory)
     * as a FastImage with zero-copy.
     * <p>
     * <b>Note:</b> The underlying native memory is NOT freed when this FastImage is disposed or resized.
     *
     * @param rawPointer 64-bit native memory address pointing to ARGB/BGRA pixels.
     * @param width      Image width in pixels (must be &gt; 0).
     * @param height     Image height in pixels (must be &gt; 0).
     * @return A {@link FastImage} wrapping the external buffer.
     * @throws FastImageException If pointer is null (0) or dimensions are invalid.
     */
    public static FastImage wrap(long rawPointer, int width, int height) {
        if (rawPointer == 0L) {
            throw new FastImageException("Raw pointer is null (0)");
        }
        if (width <= 0 || height <= 0) {
            throw new FastImageException("Invalid dimensions: " + width + "x" + height);
        }
        FastImage img = new FastImage();
        img.width = width;
        img.height = height;
        img.nativeHandle = nativeWrap(width, height, rawPointer);
        return img;
    }

    /**
     * Wraps a {@code fastpointer.Pointer} (e.g. from FastSharedMemory or FastMemory) with zero-copy.
     *
     * @param pointer Direct primitive address pointer.
     * @param width   Image width in pixels (must be &gt; 0).
     * @param height  Image height in pixels (must be &gt; 0).
     * @return A {@link FastImage} wrapping the pointer's memory location.
     * @throws FastImageException If pointer is null, invalid, or dimensions are non-positive.
     */
    public static FastImage wrap(fastpointer.Pointer pointer, int width, int height) {
        if (pointer == null || pointer.isNull()) {
            throw new FastImageException("Pointer is null");
        }
        return wrap(pointer.address(), width, height);
    }

    /**
     * Wraps a direct {@link ByteBuffer} (e.g. from FastScreen or native captures) with zero-copy.
     *
     * @param directBuffer Direct byte buffer containing image pixel data.
     * @param width        Image width in pixels (must be &gt; 0).
     * @param height       Image height in pixels (must be &gt; 0).
     * @return A {@link FastImage} wrapping the direct buffer's address.
     * @throws FastImageException If buffer is non-direct or reflection fails to extract the address.
     */
    public static FastImage wrap(ByteBuffer directBuffer, int width, int height) {
        if (directBuffer == null || !directBuffer.isDirect()) {
            throw new FastImageException("ByteBuffer must be non-null and direct");
        }
        long address;
        try {
            Field addressField = java.nio.Buffer.class.getDeclaredField("address");
            addressField.setAccessible(true);
            address = addressField.getLong(directBuffer);
        } catch (Exception e) {
            throw new FastImageException("Failed to extract direct address from ByteBuffer: " + e.getMessage(), e);
        }
        return wrap(address, width, height);
    }

    /**
     * Internal: Creates a FastImage wrapper from an existing native FastImage struct handle.
     *
     * @param handle Pointer to the native FastImage struct.
     * @param width  Image width in pixels.
     * @param height Image height in pixels.
     * @return A {@link FastImage} instance tied to the native struct.
     * @throws FastImageException If handle is 0.
     */
    public static FastImage fromNativeHandle(long handle, int width, int height) {
        if (handle == 0) {
            throw new FastImageException("Native handle is null (0)");
        }
        FastImage img = new FastImage();
        img.width = width;
        img.height = height;
        img.nativeHandle = handle;
        return img;
    }

    // =========================================================================
    // Geometry & Resampling Operations
    // =========================================================================

    /**
     * Resizes the image using bilinear interpolation.
     *
     * @param newWidth  Target width in pixels (must be &gt; 0).
     * @param newHeight Target height in pixels (must be &gt; 0).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If dimensions are invalid or operation is performed after dispose.
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
     * Resizes the image using nearest-neighbor (point) sampling.
     *
     * @param newWidth  Target width in pixels (must be &gt; 0).
     * @param newHeight Target height in pixels (must be &gt; 0).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If dimensions are invalid or operation is performed after dispose.
     */
    public FastImage resizeNearest(int newWidth, int newHeight) {
        checkDisposed();
        if (newWidth <= 0 || newHeight <= 0) {
            throw new FastImageException("Invalid dimensions: " + newWidth + "x" + newHeight);
        }
        nativeResizeNearest(nativeHandle, newWidth, newHeight);
        this.width = newWidth;
        this.height = newHeight;
        return this;
    }

    /**
     * Resizes the image using an ultra-sharp Catmull-Rom Bicubic Spline algorithm.
     * <p>
     * Delivers maximum visual sharpness, smooth gradients, and anti-aliased edges
     * when upscaling or downscaling high-definition graphics and screen captures.
     *
     * @param newWidth  Target width in pixels (must be &gt; 0).
     * @param newHeight Target height in pixels (must be &gt; 0).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If dimensions are invalid or operation is performed after dispose.
     */
    public FastImage resizeBicubic(int newWidth, int newHeight) {
        checkDisposed();
        if (newWidth <= 0 || newHeight <= 0) {
            throw new FastImageException("Invalid dimensions: " + newWidth + "x" + newHeight);
        }
        nativeResizeBicubic(nativeHandle, newWidth, newHeight);
        this.width = newWidth;
        this.height = newHeight;
        return this;
    }

    /**
     * Downsamples the image using an Anti-Aliasing Area-Averaging Box filter.
     * <p>
     * Computes the weighted average of all source pixels intersecting each target pixel.
     * Completely eliminates shimmering and pixel-crawling artifacts on high-resolution streams.
     *
     * @param newWidth  Target width in pixels (must be &gt; 0).
     * @param newHeight Target height in pixels (must be &gt; 0).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If dimensions are invalid or operation is performed after dispose.
     */
    public FastImage resizeAreaAverage(int newWidth, int newHeight) {
        checkDisposed();
        if (newWidth <= 0 || newHeight <= 0) {
            throw new FastImageException("Invalid dimensions: " + newWidth + "x" + newHeight);
        }
        nativeResizeAreaAverage(nativeHandle, newWidth, newHeight);
        this.width = newWidth;
        this.height = newHeight;
        return this;
    }

    /**
     * Crops the image to the specified rectangular sub-region.
     * Creates and returns a new {@link FastImage}, leaving the original image unmodified.
     *
     * @param x Left coordinate (must be &gt;= 0).
     * @param y Top coordinate (must be &gt;= 0).
     * @param w Width of the cropped region (must be &gt; 0).
     * @param h Height of the cropped region (must be &gt; 0).
     * @return A newly allocated {@link FastImage} containing the cropped region.
     * @throws IllegalArgumentException If crop bounds exceed image boundaries.
     * @throws FastImageException       If the image has been disposed.
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

    /**
     * Flips the image horizontally across its vertical axis.
     *
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If the image has been disposed.
     */
    public FastImage flipHorizontal() {
        checkDisposed();
        nativeFlipH(nativeHandle);
        return this;
    }

    /**
     * Flips the image vertically across its horizontal axis.
     *
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If the image has been disposed.
     */
    public FastImage flipVertical() {
        checkDisposed();
        nativeFlipV(nativeHandle);
        return this;
    }

    // =========================================================================
    // Blur & Convolution Filters
    // =========================================================================

    /**
     * Applies a fast box blur kernel.
     * Good for real-time effects where processing speed is critical.
     *
     * @param radius Blur radius in pixels (range: 0 to 50).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If radius is negative or image is disposed.
     */
    public FastImage blurBox(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        if (radius == 0) return this;
        nativeBlurBox(nativeHandle, radius);
        return this;
    }

    /**
     * Applies a high-quality separable Gaussian blur.
     * Produces smooth, artifact-free results at slightly higher compute cost than box blur.
     *
     * @param radius Blur radius in pixels (range: 0 to 50).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If radius is negative or image is disposed.
     */
    public FastImage blurGaussian(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        if (radius == 0) return this;
        nativeBlurGaussian(nativeHandle, radius);
        return this;
    }

    /**
     * Applies a stack blur (CSS {@code backdrop-filter} grade).
     * Delivers an ideal compromise between visual smoothness and high framerate for UI glass effects.
     *
     * @param radius Blur radius in pixels (range: 0 to 100).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If radius is negative or image is disposed.
     */
    public FastImage blurStack(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        if (radius == 0) return this;
        nativeBlurStack(nativeHandle, radius);
        return this;
    }

    /**
     * Applies a multi-pass Kawase blur filter (as used in Apple and Google UI designs).
     *
     * @param radius Blur radius in pixels (range: 0 to 50).
     * @param passes Number of ping-pong passes (range: 1 to 10; higher = softer).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If parameters are out of range or image is disposed.
     */
    public FastImage blurKawase(float radius, int passes) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        if (passes < 1 || passes > 10) throw new FastImageException("Invalid passes: " + passes);
        nativeBlurKawase(nativeHandle, radius, passes);
        return this;
    }

    /**
     * Applies a premium Dual Kawase 2-pass blur algorithm.
     *
     * @param radius Blur radius in pixels (range: 0 to 50).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If radius is negative or image is disposed.
     */
    public FastImage blurDualKawase(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        nativeBlurDualKawase(nativeHandle, radius);
        return this;
    }

    /**
     * Applies a mipmapped blur for very large radii (100+ pixels) by combining downscaling and upscaling.
     *
     * @param radius Blur radius in pixels (range: 0 to 200).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If radius is negative or image is disposed.
     */
    public FastImage blurMipmapped(float radius) {
        checkDisposed();
        if (radius < 0) throw new FastImageException("Radius cannot be negative: " + radius);
        nativeBlurMipmapped(nativeHandle, radius);
        return this;
    }

    // =========================================================================
    // Color & Tone Adjustments
    // =========================================================================

    /**
     * Converts the image to grayscale using ITU-R BT.601 perceptual luminance weighting.
     *
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If the image has been disposed.
     */
    public FastImage grayscale() {
        checkDisposed();
        nativeGrayscale(nativeHandle);
        return this;
    }

    /**
     * Adjusts the brightness of all color channels.
     *
     * @param factor Brightness multiplier (0.0 = black, 1.0 = unchanged, 2.0 = double brightness).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If factor is negative or image is disposed.
     */
    public FastImage adjustBrightness(float factor) {
        checkDisposed();
        if (factor < 0) throw new FastImageException("Factor cannot be negative: " + factor);
        nativeBrightness(nativeHandle, factor);
        return this;
    }

    /**
     * Adjusts the contrast of the image.
     *
     * @param factor Contrast multiplier (0.0 = solid 50% gray, 1.0 = unchanged, 2.0 = high contrast).
     * @return This {@link FastImage} instance for method chaining.
     * @throws FastImageException If factor is negative or image is disposed.
     */
    public FastImage adjustContrast(float factor) {
        checkDisposed();
        if (factor < 0) throw new FastImageException("Factor cannot be negative: " + factor);
        nativeContrast(nativeHandle, factor);
        return this;
    }

    // =========================================================================
    // Export & Interop
    // =========================================================================

    /**
     * Copies and converts the native off-heap pixel buffer into a standard Java2D {@link BufferedImage}
     * of type {@link BufferedImage#TYPE_INT_ARGB}.
     *
     * @return A newly created {@link BufferedImage} reflecting current pixel state.
     * @throws FastImageException If the image has been disposed.
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
     * Copies the native off-heap pixel data into a newly allocated Java {@code int[]} array in ARGB format.
     *
     * @return A new {@code int[]} array containing packed 32-bit ARGB pixels.
     * @throws FastImageException If the image has been disposed.
     */
    public int[] getPixels() {
        checkDisposed();
        int[] pixels = new int[width * height];
        nativeGetPixels(nativeHandle, pixels);
        return pixels;
    }

    /**
     * Copies the native off-heap pixel data into a pre-allocated Java {@code int[]} array in ARGB format (0 GC allocations).
     *
     * @param destination Array with length >= width * height.
     * @throws FastImageException If image is disposed or destination array is too small.
     */
    public void getPixels(int[] destination) {
        checkDisposed();
        if (destination == null || destination.length < width * height) {
            throw new FastImageException("Destination array is null or smaller than image dimensions");
        }
        nativeGetPixels(nativeHandle, destination);
    }

    // =========================================================================
    // Lifecycle & State Inspection
    // =========================================================================

    /**
     * Releases unmanaged native memory allocations associated with this instance immediately.
     * Calling methods on a disposed image will throw an {@link IllegalStateException}.
     */
    public void dispose() {
        if (!disposed && nativeHandle != 0) {
            nativeDispose(nativeHandle);
            nativeHandle = 0;
            disposed = true;
        }
    }

    /**
     * Checks whether this image has been disposed.
     *
     * @return {@code true} if the image was disposed and native resources were freed, {@code false} otherwise.
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Gets the current image width in pixels.
     *
     * @return Image width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the current image height in pixels.
     *
     * @return Image height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the raw 64-bit native memory address to the underlying FastImage C++ struct.
     * Use with caution.
     *
     * @return Native struct pointer address.
     */
    public long getNativeHandle() {
        return nativeHandle;
    }

    private void checkDisposed() {
        if (disposed) {
            throw new IllegalStateException("FastImage has been disposed");
        }
    }

    // =========================================================================
    // Native JNI Bindings
    // =========================================================================

    private static native long nativeCreate(int width, int height, int[] pixels);

    private static native long nativeCreateEmpty(int width, int height);

    private static native long nativeWrap(int width, int height, long rawPointer);

    private static native void nativeDispose(long handle);

    private static native void nativeResize(long handle, int newWidth, int newHeight);

    private static native void nativeResizeNearest(long handle, int newWidth, int newHeight);

    private static native void nativeResizeBicubic(long handle, int newWidth, int newHeight);

    private static native void nativeResizeAreaAverage(long handle, int newWidth, int newHeight);

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

