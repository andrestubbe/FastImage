/**
 * FastImage - Native SIMD-accelerated image processing
 * 
 * Optimized for x64 with SSE2/AVX where available.
 * Fallback to plain C++ for compatibility.
 */

#include <jni.h>
#define NOMINMAX  // Disable Windows min/max macros to avoid conflicts with std::min/max
#include <windows.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <algorithm>  // std::min, std::max
#include <emmintrin.h>  // SSE2

// Debug output
#include <stdio.h>

// Image structure
struct FastImage {
    int width;
    int height;
    int* pixels;  // ARGB format, aligned for SIMD
    bool owned;   // true if we allocated the buffer
};

// Aligned allocation for SIMD
inline int* alignedAlloc(int count) {
    return (int*)_aligned_malloc(count * sizeof(int), 16);
}

inline void alignedFree(int* ptr) {
    _aligned_free(ptr);
}

// JNI Function Implementations

extern "C" {

JNIEXPORT jlong JNICALL Java_fastimage_FastImage_nativeCreate(
    JNIEnv* env, jclass, jint width, jint height, jintArray pixels) {
    
    FastImage* img = new FastImage();
    img->width = width;
    img->height = height;
    img->pixels = alignedAlloc(width * height);
    img->owned = true;
    
    // Copy pixel data
    jint* srcPixels = env->GetIntArrayElements(pixels, nullptr);
    memcpy(img->pixels, srcPixels, width * height * sizeof(int));
    env->ReleaseIntArrayElements(pixels, srcPixels, JNI_ABORT);
    return (jlong)img;
}

JNIEXPORT jlong JNICALL Java_fastimage_FastImage_nativeCreateEmpty(
    JNIEnv* env, jclass, jint width, jint height) {
    
    FastImage* img = new FastImage();
    img->width = width;
    img->height = height;
    img->pixels = alignedAlloc(width * height);
    img->owned = true;
    memset(img->pixels, 0, width * height * sizeof(int));
    
    return (jlong)img;
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeDispose(
    JNIEnv*, jclass, jlong handle) {
    
    FastImage* img = (FastImage*)handle;
    if (img) {
        if (img->owned && img->pixels) {
            alignedFree(img->pixels);
        }
        delete img;
    }
}

// === SIMD Operations ===

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeGrayscale(
    JNIEnv*, jclass, jlong handle) {
    
    FastImage* img = (FastImage*)handle;
    int count = img->width * img->height;
    int* pixels = img->pixels;
    
    // SSE2 optimized grayscale
    int i = 0;
    for (; i <= count - 4; i += 4) {
        __m128i argb = _mm_loadu_si128((__m128i*)(pixels + i));
        
        // Extract channels (ARGB → process RGB, keep A)
        // Simplified: use average of RGB for speed
        __m128i r = _mm_and_si128(_mm_srli_epi32(argb, 16), _mm_set1_epi32(0xFF));
        __m128i g = _mm_and_si128(_mm_srli_epi32(argb, 8), _mm_set1_epi32(0xFF));
        __m128i b = _mm_and_si128(argb, _mm_set1_epi32(0xFF));
        __m128i a = _mm_and_si128(_mm_srli_epi32(argb, 24), _mm_set1_epi32(0xFF));
        
        // Gray = (R + G + G + B) / 4  (fast approximation)
        __m128i gray = _mm_srli_epi32(_mm_add_epi32(_mm_add_epi32(r, _mm_add_epi32(g, g)), b), 2);
        
        // Reconstruct ARGB with gray
        __m128i result = _mm_or_si128(
            _mm_slli_epi32(a, 24),
            _mm_or_si128(
                _mm_slli_epi32(gray, 16),
                _mm_or_si128(
                    _mm_slli_epi32(gray, 8),
                    gray
                )
            )
        );
        
        _mm_storeu_si128((__m128i*)(pixels + i), result);
    }
    
    // Handle remaining pixels
    for (; i < count; i++) {
        int pixel = pixels[i];
        int a = (pixel >> 24) & 0xFF;
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;
        int gray = (r + g + g + b) >> 2;  // Fast approximation
        pixels[i] = (a << 24) | (gray << 16) | (gray << 8) | gray;
    }
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBrightness(
    JNIEnv*, jclass, jlong handle, jfloat factor) {
    
    FastImage* img = (FastImage*)handle;
    int count = img->width * img->height;
    int* pixels = img->pixels;
    
    int scale = (int)(factor * 256.0f);
    
    for (int i = 0; i < count; i++) {
        int pixel = pixels[i];
        int a = (pixel >> 24) & 0xFF;
        int r = ((pixel >> 16) & 0xFF) * scale >> 8;
        int g = ((pixel >> 8) & 0xFF) * scale >> 8;
        int b = (pixel & 0xFF) * scale >> 8;
        
        r = r > 255 ? 255 : r;
        g = g > 255 ? 255 : g;
        b = b > 255 ? 255 : b;
        
        pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
    }
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeContrast(
    JNIEnv*, jclass, jlong handle, jfloat factor) {
    
    FastImage* img = (FastImage*)handle;
    int count = img->width * img->height;
    int* pixels = img->pixels;
    
    int scale = (int)(factor * 256.0f);
    int offset = (int)(128.0f * (1.0f - factor) * 256.0f);
    
    for (int i = 0; i < count; i++) {
        int pixel = pixels[i];
        int a = (pixel >> 24) & 0xFF;
        int r = ((pixel >> 16) & 0xFF);
        int g = ((pixel >> 8) & 0xFF);
        int b = (pixel & 0xFF);
        
        r = ((r - 128) * scale >> 8) + 128;
        g = ((g - 128) * scale >> 8) + 128;
        b = ((b - 128) * scale >> 8) + 128;
        
        r = r < 0 ? 0 : (r > 255 ? 255 : r);
        g = g < 0 ? 0 : (g > 255 ? 255 : g);
        b = b < 0 ? 0 : (b > 255 ? 255 : b);
        
        pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
    }
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeFlipH(
    JNIEnv*, jclass, jlong handle) {
    
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    
    for (int y = 0; y < h; y++) {
        int* row = pixels + y * w;
        for (int x = 0; x < w / 2; x++) {
            int tmp = row[x];
            row[x] = row[w - 1 - x];
            row[w - 1 - x] = tmp;
        }
    }
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeFlipV(
    JNIEnv*, jclass, jlong handle) {
    
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    
    int* temp = (int*)malloc(w * sizeof(int));
    
    for (int y = 0; y < h / 2; y++) {
        int* row1 = pixels + y * w;
        int* row2 = pixels + (h - 1 - y) * w;
        memcpy(temp, row1, w * sizeof(int));
        memcpy(row1, row2, w * sizeof(int));
        memcpy(row2, temp, w * sizeof(int));
    }
    
    free(temp);
}

JNIEXPORT jlong JNICALL Java_fastimage_FastImage_nativeCrop(
    JNIEnv*, jclass, jlong handle, jint x, jint y, jint w, jint h) {
    
    FastImage* src = (FastImage*)handle;
    FastImage* dst = new FastImage();
    dst->width = w;
    dst->height = h;
    dst->pixels = alignedAlloc(w * h);
    dst->owned = true;
    
    for (int row = 0; row < h; row++) {
        memcpy(dst->pixels + row * w,
               src->pixels + (y + row) * src->width + x,
               w * sizeof(int));
    }
    
    return (jlong)dst;
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeResize(
    JNIEnv*, jclass, jlong handle, jint newWidth, jint newHeight) {
    
    FastImage* src = (FastImage*)handle;
    int oldW = src->width;
    int oldH = src->height;
    int* oldPixels = src->pixels;
    
    // Allocate new buffer
    int* newPixels = alignedAlloc(newWidth * newHeight);
    
    // Bilinear interpolation
    float xRatio = (float)(oldW - 1) / newWidth;
    float yRatio = (float)(oldH - 1) / newHeight;
    
    for (int y = 0; y < newHeight; y++) {
        for (int x = 0; x < newWidth; x++) {
            float px = x * xRatio;
            float py = y * yRatio;
            
            int x1 = (int)px;
            int y1 = (int)py;
            int x2 = x1 + 1 < oldW ? x1 + 1 : x1;
            int y2 = y1 + 1 < oldH ? y1 + 1 : y1;
            
            float fx = px - x1;
            float fy = py - y1;
            
            // Get 4 pixels
            int p11 = oldPixels[y1 * oldW + x1];
            int p12 = oldPixels[y1 * oldW + x2];
            int p21 = oldPixels[y2 * oldW + x1];
            int p22 = oldPixels[y2 * oldW + x2];
            
            // Interpolate each channel
            auto lerp = [](int c1, int c2, float f) -> int {
                return (int)(c1 + f * (c2 - c1));
            };
            
            int a = lerp(lerp((p11 >> 24) & 0xFF, (p12 >> 24) & 0xFF, fx),
                        lerp((p21 >> 24) & 0xFF, (p22 >> 24) & 0xFF, fx), fy);
            int r = lerp(lerp((p11 >> 16) & 0xFF, (p12 >> 16) & 0xFF, fx),
                        lerp((p21 >> 16) & 0xFF, (p22 >> 16) & 0xFF, fx), fy);
            int g = lerp(lerp((p11 >> 8) & 0xFF, (p12 >> 8) & 0xFF, fx),
                        lerp((p21 >> 8) & 0xFF, (p22 >> 8) & 0xFF, fx), fy);
            int b = lerp(lerp(p11 & 0xFF, p12 & 0xFF, fx),
                        lerp(p21 & 0xFF, p22 & 0xFF, fx), fy);
            
            newPixels[y * newWidth + x] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
    
    // Replace old buffer
    alignedFree(oldPixels);
    src->pixels = newPixels;
    src->width = newWidth;
    src->height = newHeight;
}

// Simple box blur - fast approximation
JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurBox(
    JNIEnv*, jclass, jlong handle, jfloat radius) {
    
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    
    int r = (int)(radius + 0.5f);
    if (r < 1) r = 1;
    if (r > 50) r = 50;  // Limit for performance
    
    int* temp = alignedAlloc(w * h);
    memcpy(temp, pixels, w * h * sizeof(int));
    
    // Horizontal pass
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            int a = 0, rsum = 0, g = 0, b = 0;
            int count = 0;
            
            for (int k = -r; k <= r; k++) {
                int sx = x + k;
                if (sx >= 0 && sx < w) {
                    int p = temp[y * w + sx];
                    a += (p >> 24) & 0xFF;
                    rsum += (p >> 16) & 0xFF;
                    g += (p >> 8) & 0xFF;
                    b += p & 0xFF;
                    count++;
                }
            }
            
            pixels[y * w + x] = ((a / count) << 24) | 
                               ((rsum / count) << 16) | 
                               ((g / count) << 8) | 
                               (b / count);
        }
    }
    
    memcpy(temp, pixels, w * h * sizeof(int));
    
    // Vertical pass
    for (int x = 0; x < w; x++) {
        for (int y = 0; y < h; y++) {
            int a = 0, rsum = 0, g = 0, b = 0;
            int count = 0;
            
            for (int k = -r; k <= r; k++) {
                int sy = y + k;
                if (sy >= 0 && sy < h) {
                    int p = temp[sy * w + x];
                    a += (p >> 24) & 0xFF;
                    rsum += (p >> 16) & 0xFF;
                    g += (p >> 8) & 0xFF;
                    b += p & 0xFF;
                    count++;
                }
            }
            
            pixels[y * w + x] = ((a / count) << 24) | 
                               ((rsum / count) << 16) | 
                               ((g / count) << 8) | 
                               (b / count);
        }
    }
    
    alignedFree(temp);
}

// Separable Gaussian blur - higher quality than box blur
JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurGaussian(
    JNIEnv*, jclass, jlong handle, jfloat radius) {
    
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    
    int r = (int)(radius + 0.5f);
    if (r < 1) r = 1;
    if (r > 50) r = 50;
    
    // Create Gaussian kernel
    int kernelSize = 2 * r + 1;
    float* kernel = new float[kernelSize];
    float sigma = radius / 2.0f;
    float twoSigmaSq = 2.0f * sigma * sigma;
    float sum = 0.0f;
    
    for (int i = 0; i < kernelSize; i++) {
        int x = i - r;
        kernel[i] = (float)exp(-(x * x) / twoSigmaSq);
        sum += kernel[i];
    }
    // Normalize
    for (int i = 0; i < kernelSize; i++) {
        kernel[i] /= sum;
    }
    
    int* temp = alignedAlloc(w * h);
    memcpy(temp, pixels, w * h * sizeof(int));
    
    // Horizontal pass with Gaussian kernel
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            float a = 0, rsum = 0, g = 0, b = 0;
            
            for (int k = -r; k <= r; k++) {
                int sx = x + k;
                int ki = k + r;
                if (sx >= 0 && sx < w) {
                    int p = temp[y * w + sx];
                    float weight = kernel[ki];
                    a += ((p >> 24) & 0xFF) * weight;
                    rsum += ((p >> 16) & 0xFF) * weight;
                    g += ((p >> 8) & 0xFF) * weight;
                    b += (p & 0xFF) * weight;
                }
            }
            
            pixels[y * w + x] = (((int)a << 24) & 0xFF000000) | 
                               (((int)rsum << 16) & 0x00FF0000) | 
                               (((int)g << 8) & 0x0000FF00) | 
                               ((int)b & 0x000000FF);
        }
    }
    
    memcpy(temp, pixels, w * h * sizeof(int));
    
    // Vertical pass with Gaussian kernel
    for (int x = 0; x < w; x++) {
        for (int y = 0; y < h; y++) {
            float a = 0, rsum = 0, g = 0, b = 0;
            
            for (int k = -r; k <= r; k++) {
                int sy = y + k;
                int ki = k + r;
                if (sy >= 0 && sy < h) {
                    int p = temp[sy * w + x];
                    float weight = kernel[ki];
                    a += ((p >> 24) & 0xFF) * weight;
                    rsum += ((p >> 16) & 0xFF) * weight;
                    g += ((p >> 8) & 0xFF) * weight;
                    b += (p & 0xFF) * weight;
                }
            }
            
            pixels[y * w + x] = (((int)a << 24) & 0xFF000000) | 
                               (((int)rsum << 16) & 0x00FF0000) | 
                               (((int)g << 8) & 0x0000FF00) | 
                               ((int)b & 0x000000FF);
        }
    }
    
    delete[] kernel;
    alignedFree(temp);
}

// Stack blur - CSS backdrop-filter style, fast with good quality
JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurStack(
    JNIEnv*, jclass, jlong handle, jfloat radius) {
    
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    
    int r = (int)(radius + 0.5f);
    if (r < 1) r = 1;
    if (r > 100) r = 100;
    
    int* temp = alignedAlloc(w * h);
    memcpy(temp, pixels, w * h * sizeof(int));
    
    int div = r + r + 1;
    int* rBuffer = new int[w];
    int* gBuffer = new int[w];
    int* bBuffer = new int[w];
    int* aBuffer = new int[w];
    
    // Horizontal pass
    for (int y = 0; y < h; y++) {
        int rsum = 0, gsum = 0, bsum = 0, asum = 0;
        
        for (int i = -r; i <= r; i++) {
            int x = std::max(0, std::min(i, w - 1));
            int p = temp[y * w + x];
            asum += (p >> 24) & 0xFF;
            rsum += (p >> 16) & 0xFF;
            gsum += (p >> 8) & 0xFF;
            bsum += p & 0xFF;
        }
        
        for (int x = 0; x < w; x++) {
            int p = temp[y * w + x];
            pixels[y * w + x] = ((asum / div) << 24) | 
                               ((rsum / div) << 16) | 
                               ((gsum / div) << 8) | 
                               (bsum / div);
            
            // Slide window
            int pOut = (x - r > 0) ? temp[y * w + (x - r - 1)] : temp[y * w];
            int pIn = (x + r + 1 < w) ? temp[y * w + (x + r + 1)] : temp[y * w + (w - 1)];
            
            asum -= ((pOut >> 24) & 0xFF) - ((pIn >> 24) & 0xFF);
            rsum -= ((pOut >> 16) & 0xFF) - ((pIn >> 16) & 0xFF);
            gsum -= ((pOut >> 8) & 0xFF) - ((pIn >> 8) & 0xFF);
            bsum -= (pOut & 0xFF) - (pIn & 0xFF);
        }
    }
    
    memcpy(temp, pixels, w * h * sizeof(int));
    
    // Vertical pass
    for (int x = 0; x < w; x++) {
        int rsum = 0, gsum = 0, bsum = 0, asum = 0;
        
        for (int i = -r; i <= r; i++) {
            int y = std::max(0, std::min(i, h - 1));
            int p = temp[y * w + x];
            asum += (p >> 24) & 0xFF;
            rsum += (p >> 16) & 0xFF;
            gsum += (p >> 8) & 0xFF;
            bsum += p & 0xFF;
        }
        
        for (int y = 0; y < h; y++) {
            pixels[y * w + x] = ((asum / div) << 24) | 
                               ((rsum / div) << 16) | 
                               ((gsum / div) << 8) | 
                               (bsum / div);
            
            int pOut = (y - r > 0) ? temp[(y - r - 1) * w + x] : temp[x];
            int pIn = (y + r + 1 < h) ? temp[(y + r + 1) * w + x] : temp[(h - 1) * w + x];
            
            asum -= ((pOut >> 24) & 0xFF) - ((pIn >> 24) & 0xFF);
            rsum -= ((pOut >> 16) & 0xFF) - ((pIn >> 16) & 0xFF);
            gsum -= ((pOut >> 8) & 0xFF) - ((pIn >> 8) & 0xFF);
            bsum -= (pOut & 0xFF) - (pIn & 0xFF);
        }
    }
    
    delete[] rBuffer;
    delete[] gBuffer;
    delete[] bBuffer;
    delete[] aBuffer;
    alignedFree(temp);
}

// Kawase blur - multi-pass algorithm used by Apple/Google
JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurKawase(
    JNIEnv*, jclass, jlong handle, jfloat radius, jint passes) {
    
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    
    if (passes < 1) passes = 1;
    if (passes > 5) passes = 5;
    
    int* temp = alignedAlloc(w * h);
    int* src = pixels;
    int* dst = temp;
    
    float offset = radius / (float)passes;
    if (offset < 1.0f) offset = 1.0f;
    
    for (int pass = 0; pass < passes; pass++) {
        int d = (int)(offset * (pass + 1));
        if (d < 1) d = 1;
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // Sample 4 corners
                int x1 = std::max(0, x - d);
                int x2 = std::min(w - 1, x + d);
                int y1 = std::max(0, y - d);
                int y2 = std::min(h - 1, y + d);
                
                int p1 = src[y1 * w + x1];
                int p2 = src[y1 * w + x2];
                int p3 = src[y2 * w + x1];
                int p4 = src[y2 * w + x2];
                
                int a = (((p1 >> 24) & 0xFF) + ((p2 >> 24) & 0xFF) + 
                        ((p3 >> 24) & 0xFF) + ((p4 >> 24) & 0xFF)) / 4;
                int r = (((p1 >> 16) & 0xFF) + ((p2 >> 16) & 0xFF) + 
                        ((p3 >> 16) & 0xFF) + ((p4 >> 16) & 0xFF)) / 4;
                int g = (((p1 >> 8) & 0xFF) + ((p2 >> 8) & 0xFF) + 
                        ((p3 >> 8) & 0xFF) + ((p4 >> 8) & 0xFF)) / 4;
                int b = ((p1 & 0xFF) + (p2 & 0xFF) + (p3 & 0xFF) + (p4 & 0xFF)) / 4;
                
                dst[y * w + x] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        
        // Swap buffers
        int* t = src;
        src = dst;
        dst = t;
    }
    
    // Copy back if needed
    if (src != pixels) {
        memcpy(pixels, src, w * h * sizeof(int));
    }
    
    alignedFree(temp);
}

// Dual Kawase blur - premium 2-pass algorithm
JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurDualKawase(
    JNIEnv*, jclass, jlong handle, jfloat radius) {
    
    // Simply use Kawase with 2 passes for now
    Java_fastimage_FastImage_nativeBlurKawase(nullptr, nullptr, handle, radius, 2);
}

// Mipmapped blur - for very large radii using downsample + blur + upsample
JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurMipmapped(
    JNIEnv*, jclass, jlong handle, jfloat radius) {
    
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    
    // Determine mip level based on radius
    int levels = 0;
    float r = radius;
    while (r > 8.0f && levels < 3) {
        r /= 2.0f;
        levels++;
    }
    
    if (levels == 0) {
        // Small radius - use regular box blur
        Java_fastimage_FastImage_nativeBlurBox(nullptr, nullptr, handle, radius);
        return;
    }
    
    // Calculate downsampled size
    int newW = w >> levels;
    int newH = h >> levels;
    if (newW < 4) newW = 4;
    if (newH < 4) newH = 4;
    
    // Downsample (simple box filter)
    int* mipPixels = alignedAlloc(newW * newH);
    
    for (int y = 0; y < newH; y++) {
        for (int x = 0; x < newW; x++) {
            int sumA = 0, sumR = 0, sumG = 0, sumB = 0;
            int count = 0;
            
            for (int dy = 0; dy < (1 << levels); dy++) {
                for (int dx = 0; dx < (1 << levels); dx++) {
                    int sy = (y << levels) + dy;
                    int sx = (x << levels) + dx;
                    if (sy < h && sx < w) {
                        int p = pixels[sy * w + sx];
                        sumA += (p >> 24) & 0xFF;
                        sumR += (p >> 16) & 0xFF;
                        sumG += (p >> 8) & 0xFF;
                        sumB += p & 0xFF;
                        count++;
                    }
                }
            }
            
            mipPixels[y * newW + x] = ((sumA / count) << 24) | 
                                      ((sumR / count) << 16) | 
                                      ((sumG / count) << 8) | 
                                      (sumB / count);
        }
    }
    
    // Apply small blur on downsampled image
    FastImage* mipImg = new FastImage();
    mipImg->width = newW;
    mipImg->height = newH;
    mipImg->pixels = mipPixels;
    mipImg->owned = true;
    
    // Small box blur on mip
    Java_fastimage_FastImage_nativeBlurBox(nullptr, nullptr, (jlong)mipImg, r);
    
    // Upsample (bilinear)
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            float fx = (float)(x * newW) / w;
            float fy = (float)(y * newH) / h;
            int ix = (int)fx;
            int iy = (int)fy;
            float dx = fx - ix;
            float dy = fy - iy;
            
            int x1 = std::min(ix, newW - 1);
            int x2 = std::min(ix + 1, newW - 1);
            int y1 = std::min(iy, newH - 1);
            int y2 = std::min(iy + 1, newH - 1);
            
            int p1 = mipPixels[y1 * newW + x1];
            int p2 = mipPixels[y1 * newW + x2];
            int p3 = mipPixels[y2 * newW + x1];
            int p4 = mipPixels[y2 * newW + x2];
            
            int a = (int)((((p1 >> 24) & 0xFF) * (1-dx) * (1-dy) +
                          ((p2 >> 24) & 0xFF) * dx * (1-dy) +
                          ((p3 >> 24) & 0xFF) * (1-dx) * dy +
                          ((p4 >> 24) & 0xFF) * dx * dy));
            int r = (int)((((p1 >> 16) & 0xFF) * (1-dx) * (1-dy) +
                          ((p2 >> 16) & 0xFF) * dx * (1-dy) +
                          ((p3 >> 16) & 0xFF) * (1-dx) * dy +
                          ((p4 >> 16) & 0xFF) * dx * dy));
            int g = (int)((((p1 >> 8) & 0xFF) * (1-dx) * (1-dy) +
                          ((p2 >> 8) & 0xFF) * dx * (1-dy) +
                          ((p3 >> 8) & 0xFF) * (1-dx) * dy +
                          ((p4 >> 8) & 0xFF) * dx * dy));
            int b = (int)(((p1 & 0xFF) * (1-dx) * (1-dy) +
                          (p2 & 0xFF) * dx * (1-dy) +
                          (p3 & 0xFF) * (1-dx) * dy +
                          (p4 & 0xFF) * dx * dy));
            
            pixels[y * w + x] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
    
    alignedFree(mipPixels);
    delete mipImg;
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeGetPixels(
    JNIEnv* env, jclass, jlong handle, jintArray dstArray) {
    
    FastImage* img = (FastImage*)handle;
    int count = img->width * img->height;
    
    jint* dst = env->GetIntArrayElements(dstArray, nullptr);
    memcpy(dst, img->pixels, count * sizeof(int));
    env->ReleaseIntArrayElements(dstArray, dst, 0);
}

} // extern "C"
