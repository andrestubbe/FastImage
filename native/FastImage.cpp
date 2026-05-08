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
#include <iostream>
#include <vector>
#include <algorithm>
#include <cmath>
#include <immintrin.h>
#include <cstring>
#include <intrin.h> // For __cpuid

// Helper to throw Java exceptions
void throwFastImageException(JNIEnv* env, const char* message) {
    jclass exClass = env->FindClass("fastimage/FastImageException");
    if (exClass != NULL) {
        env->ThrowNew(exClass, message);
    }
}

// CPU Feature Detection
struct CPUFeatures {
    bool avx2;
    bool sse41;
    
    CPUFeatures() {
        int cpuInfo[4];
        __cpuid(cpuInfo, 1);
        sse41 = (cpuInfo[2] & (1 << 19)) != 0;
        
        __cpuidex(cpuInfo, 7, 0);
        avx2 = (cpuInfo[1] & (1 << 5)) != 0;
    }
};

static CPUFeatures g_cpu;

// Image structure
struct FastImage {
    int width;
    int height;
    int* pixels;  // ARGB format, aligned for SIMD
    bool owned;   // true if we allocated the buffer
};

// Aligned allocation for SIMD (32-byte alignment for AVX2)
inline int* alignedAlloc(int count) {
    return (int*)_aligned_malloc(count * sizeof(int), 32);
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
    JNIEnv* env, jclass, jlong handle) {
    
    if (handle == 0) {
        throwFastImageException(env, "Native handle is null");
        return;
    }
    
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    int size = w * h;
    
    if (g_cpu.avx2) {
        // AVX2 Path (32 bytes / 8 pixels at a time)
        __m256i v_r_w = _mm256_set1_epi32(77);  // 0.299 * 256
        __m256i v_g_w = _mm256_set1_epi32(150); // 0.587 * 256
        __m256i v_b_w = _mm256_set1_epi32(29);  // 0.114 * 256
        
        for (int i = 0; i <= size - 8; i += 8) {
            __m256i p = _mm256_loadu_si256((__m256i*)&pixels[i]);
            __m256i r = _mm256_and_si256(_mm256_srli_epi32(p, 16), _mm256_set1_epi32(0xFF));
            __m256i g = _mm256_and_si256(_mm256_srli_epi32(p, 8), _mm256_set1_epi32(0xFF));
            __m256i b = _mm256_and_si256(p, _mm256_set1_epi32(0xFF));
            
            __m256i gray = _mm256_srli_epi32(_mm256_add_epi32(_mm256_add_epi32(
                _mm256_mullo_epi32(r, v_r_w), _mm256_mullo_epi32(g, v_g_w)), 
                _mm256_mullo_epi32(b, v_b_w)), 8);
            
            __m256i res = _mm256_or_si256(_mm256_and_si256(p, _mm256_set1_epi32(0xFF000000)),
                          _mm256_or_si256(_mm256_slli_epi32(gray, 16),
                          _mm256_or_si256(_mm256_slli_epi32(gray, 8), gray)));
            _mm256_storeu_si256((__m256i*)&pixels[i], res);
        }
    } else {
        // SSE4.1 Fallback
        __m128i v_r_w = _mm_set1_epi32(77);
        __m128i v_g_w = _mm_set1_epi32(150);
        __m128i v_b_w = _mm_set1_epi32(29);
        
        for (int i = 0; i <= size - 4; i += 4) {
            __m128i p = _mm_loadu_si128((__m128i*)&pixels[i]);
            __m128i r = _mm_and_si128(_mm_srli_epi32(p, 16), _mm_set1_epi32(0xFF));
            __m128i g = _mm_and_si128(_mm_srli_epi32(p, 8), _mm_set1_epi32(0xFF));
            __m128i b = _mm_and_si128(p, _mm_set1_epi32(0xFF));
            
            __m128i gray = _mm_srli_epi32(_mm_add_epi32(_mm_add_epi32(
                _mm_mullo_epi32(r, v_r_w), _mm_mullo_epi32(g, v_g_w)), 
                _mm_mullo_epi32(b, v_b_w)), 8);
                
            __m128i res = _mm_or_si128(_mm_and_si128(p, _mm_set1_epi32(0xFF000000)),
                          _mm_or_si128(_mm_slli_epi32(gray, 16),
                          _mm_or_si128(_mm_slli_epi32(gray, 8), gray)));
            _mm_storeu_si128((__m128i*)&pixels[i], res);
        }
    }
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBrightness(
    JNIEnv* env, jclass, jlong handle, jfloat factor) {
    
    if (handle == 0) {
        throwFastImageException(env, "Native handle is null");
        return;
    }
    
    FastImage* img = (FastImage*)handle;
    int size = img->width * img->height;
    int* pixels = img->pixels;
    
    if (g_cpu.avx2) {
        // AVX2 Path
        __m256 v_factor = _mm256_set1_ps(factor);
        __m256 v_255 = _mm256_set1_ps(255.0f);
        __m256 v_0 = _mm256_setzero_ps();
        __m256i v_mask = _mm256_set1_epi32(0xFF);

        for (int i = 0; i <= size - 8; i += 8) {
            __m256i p = _mm256_loadu_si256((__m256i*)&pixels[i]);
            
            // Extract channels and convert to float
            __m256 r = _mm256_cvtepi32_ps(_mm256_and_si256(_mm256_srli_epi32(p, 16), v_mask));
            __m256 g = _mm256_cvtepi32_ps(_mm256_and_si256(_mm256_srli_epi32(p, 8), v_mask));
            __m256 b = _mm256_cvtepi32_ps(_mm256_and_si256(p, v_mask));
            
            // Multiply and clamp
            r = _mm256_min_ps(_mm256_mul_ps(r, v_factor), v_255);
            g = _mm256_min_ps(_mm256_mul_ps(g, v_factor), v_255);
            b = _mm256_min_ps(_mm256_mul_ps(b, v_factor), v_255);
            
            // Convert back and pack
            __m256i ri = _mm256_cvtps_epi32(r);
            __m256i gi = _mm256_cvtps_epi32(g);
            __m256i bi = _mm256_cvtps_epi32(b);
            
            __m256i res = _mm256_or_si256(_mm256_and_si256(p, _mm256_set1_epi32(0xFF000000)),
                          _mm256_or_si256(_mm256_slli_epi32(ri, 16),
                          _mm256_or_si256(_mm256_slli_epi32(gi, 8), bi)));
            _mm256_storeu_si256((__m256i*)&pixels[i], res);
        }
    } else {
        // SSE4.1 Fallback
        __m128 v_factor = _mm_set1_ps(factor);
        __m128 v_255 = _mm_set1_ps(255.0f);
        __m128i mask = _mm_set1_epi32(0xFF);
        
        for (int i = 0; i <= size - 4; i += 4) {
            __m128i p = _mm_loadu_si128((__m128i*)&pixels[i]);
            __m128 r = _mm_cvtepi32_ps(_mm_and_si128(_mm_srli_epi32(p, 16), mask));
            __m128 g = _mm_cvtepi32_ps(_mm_and_si128(_mm_srli_epi32(p, 8), mask));
            __m128 b = _mm_cvtepi32_ps(_mm_and_si128(p, mask));
            
            r = _mm_min_ps(_mm_mul_ps(r, v_factor), v_255);
            g = _mm_min_ps(_mm_mul_ps(g, v_factor), v_255);
            b = _mm_min_ps(_mm_mul_ps(b, v_factor), v_255);
            
            __m128i ri = _mm_cvtps_epi32(r);
            __m128i gi = _mm_cvtps_epi32(g);
            __m128i bi = _mm_cvtps_epi32(b);
            
            __m128i res = _mm_or_si128(_mm_and_si128(p, _mm_set1_epi32(0xFF000000)),
                          _mm_or_si128(_mm_slli_epi32(ri, 16),
                          _mm_or_si128(_mm_slli_epi32(gi, 8), bi)));
            _mm_storeu_si128((__m128i*)&pixels[i], res);
        }
    }
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeContrast(
    JNIEnv* env, jclass, jlong handle, jfloat factor) {
    
    if (handle == 0) {
        throwFastImageException(env, "Native handle is null");
        return;
    }
    
    FastImage* img = (FastImage*)handle;
    int size = img->width * img->height;
    int* pixels = img->pixels;
    
    if (g_cpu.avx2) {
        // AVX2 Path
        __m256 v_f = _mm256_set1_ps(factor);
        __m256 v_128 = _mm256_set1_ps(128.0f);
        __m256 v_255 = _mm256_set1_ps(255.0f);
        __m256 v_0 = _mm256_setzero_ps();
        __m256i v_m = _mm256_set1_epi32(0xFF);

        for (int i = 0; i <= size - 8; i += 8) {
            __m256i p = _mm256_loadu_si256((__m256i*)&pixels[i]);
            __m256 r = _mm256_cvtepi32_ps(_mm256_and_si256(_mm256_srli_epi32(p, 16), v_m));
            __m256 g = _mm256_cvtepi32_ps(_mm256_and_si256(_mm256_srli_epi32(p, 8), v_m));
            __m256 b = _mm256_cvtepi32_ps(_mm256_and_si256(p, v_m));
            
            r = _mm256_min_ps(_mm256_max_ps(_mm256_add_ps(_mm256_mul_ps(_mm256_sub_ps(r, v_128), v_f), v_128), v_0), v_255);
            g = _mm256_min_ps(_mm256_max_ps(_mm256_add_ps(_mm256_mul_ps(_mm256_sub_ps(g, v_128), v_f), v_128), v_0), v_255);
            b = _mm256_min_ps(_mm256_max_ps(_mm256_add_ps(_mm256_mul_ps(_mm256_sub_ps(b, v_128), v_f), v_128), v_0), v_255);
            
            __m256i ri = _mm256_cvtps_epi32(r);
            __m256i gi = _mm256_cvtps_epi32(g);
            __m256i bi = _mm256_cvtps_epi32(b);
            
            __m256i res = _mm256_or_si256(_mm256_and_si256(p, _mm256_set1_epi32(0xFF000000)),
                          _mm256_or_si256(_mm256_slli_epi32(ri, 16),
                          _mm256_or_si256(_mm256_slli_epi32(gi, 8), bi)));
            _mm256_storeu_si256((__m256i*)&pixels[i], res);
        }
    } else {
        // SSE4.1 Fallback
        __m128 v_f = _mm_set1_ps(factor);
        __m128 v_128 = _mm_set1_ps(128.0f);
        __m128 v_255 = _mm_set1_ps(255.0f);
        __m128 v_0 = _mm_setzero_ps();
        __m128i v_m = _mm_set1_epi32(0xFF);
        
        for (int i = 0; i <= size - 4; i += 4) {
            __m128i p = _mm_loadu_si128((__m128i*)&pixels[i]);
            __m128 r = _mm_cvtepi32_ps(_mm_and_si128(_mm_srli_epi32(p, 16), v_m));
            __m128 g = _mm_cvtepi32_ps(_mm_and_si128(_mm_srli_epi32(p, 8), v_m));
            __m128 b = _mm_cvtepi32_ps(_mm_and_si128(p, v_m));
            
            r = _mm_min_ps(_mm_max_ps(_mm_add_ps(_mm_mul_ps(_mm_sub_ps(r, v_128), v_f), v_128), v_0), v_255);
            g = _mm_min_ps(_mm_max_ps(_mm_add_ps(_mm_mul_ps(_mm_sub_ps(g, v_128), v_f), v_128), v_0), v_255);
            b = _mm_min_ps(_mm_max_ps(_mm_add_ps(_mm_mul_ps(_mm_sub_ps(b, v_128), v_f), v_128), v_0), v_255);
            
            __m128i ri = _mm_cvtps_epi32(r);
            __m128i gi = _mm_cvtps_epi32(g);
            __m128i bi = _mm_cvtps_epi32(b);
            
            __m128i res = _mm_or_si128(_mm_and_si128(p, _mm_set1_epi32(0xFF000000)),
                          _mm_or_si128(_mm_slli_epi32(ri, 16),
                          _mm_or_si128(_mm_slli_epi32(gi, 8), bi)));
            _mm_storeu_si128((__m128i*)&pixels[i], res);
        }
    }
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeFlipH(
    JNIEnv* env, jclass, jlong handle) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
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
    JNIEnv* env, jclass, jlong handle) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
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
    JNIEnv* env, jclass, jlong handle, jint x, jint y, jint w, jint h) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return 0; }
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
    JNIEnv* env, jclass, jlong handle, jint newWidth, jint newHeight) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
    FastImage* src = (FastImage*)handle;
    int oldW = src->width;
    int oldH = src->height;
    int* oldPixels = src->pixels;
    int* newPixels = alignedAlloc(newWidth * newHeight);
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
            int p11 = oldPixels[y1 * oldW + x1];
            int p12 = oldPixels[y1 * oldW + x2];
            int p21 = oldPixels[y2 * oldW + x1];
            int p22 = oldPixels[y2 * oldW + x2];
            auto lerp = [](int c1, int c2, float f) -> int { return (int)(c1 + f * (c2 - c1)); };
            int a = lerp(lerp((p11 >> 24) & 0xFF, (p12 >> 24) & 0xFF, fx), lerp((p21 >> 24) & 0xFF, (p22 >> 24) & 0xFF, fx), fy);
            int r = lerp(lerp((p11 >> 16) & 0xFF, (p12 >> 16) & 0xFF, fx), lerp((p21 >> 16) & 0xFF, (p22 >> 16) & 0xFF, fx), fy);
            int g = lerp(lerp((p11 >> 8) & 0xFF, (p12 >> 8) & 0xFF, fx), lerp((p21 >> 8) & 0xFF, (p22 >> 8) & 0xFF, fx), fy);
            int b = lerp(lerp(p11 & 0xFF, p12 & 0xFF, fx), lerp(p21 & 0xFF, p22 & 0xFF, fx), fy);
            newPixels[y * newWidth + x] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
    alignedFree(oldPixels);
    src->pixels = newPixels;
    src->width = newWidth;
    src->height = newHeight;
}

// Optimized Box Blur using sliding window (O(N))
JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurBox(
    JNIEnv* env, jclass, jlong handle, jfloat radius) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
    FastImage* img = (FastImage*)handle;
    int w = img->width;
    int h = img->height;
    int* pixels = img->pixels;
    int r = (int)(radius + 0.5f);
    if (r < 1) return;
    if (r > w / 2) r = w / 2 - 1;
    if (r > h / 2) r = h / 2 - 1;
    int* temp = alignedAlloc(w * h);
    for (int y = 0; y < h; y++) {
        int* srcRow = pixels + y * w;
        int* dstRow = temp + y * w;
        long long sumA = 0, sumR = 0, sumG = 0, sumB = 0;
        int div = 2 * r + 1;
        for (int i = -r; i <= r; i++) {
            int p = srcRow[std::max(0, std::min(i, w - 1))];
            sumA += (p >> 24) & 0xFF; sumR += (p >> 16) & 0xFF; sumG += (p >> 8) & 0xFF; sumB += p & 0xFF;
        }
        for (int x = 0; x < w; x++) {
            dstRow[x] = (int)((sumA / div) << 24) | (int)((sumR / div) << 16) | (int)((sumG / div) << 8) | (int)(sumB / div);
            int pOut = srcRow[std::max(0, x - r)]; int pIn = srcRow[std::min(w - 1, x + r + 1)];
            sumA += ((pIn >> 24) & 0xFF) - ((pOut >> 24) & 0xFF);
            sumR += ((pIn >> 16) & 0xFF) - ((pOut >> 16) & 0xFF);
            sumG += ((pIn >> 8) & 0xFF) - ((pOut >> 8) & 0xFF);
            sumB += (pIn & 0xFF) - (pOut & 0xFF);
        }
    }
    for (int x = 0; x < w; x++) {
        long long sumA = 0, sumR = 0, sumG = 0, sumB = 0;
        int div = 2 * r + 1;
        for (int i = -r; i <= r; i++) {
            int p = temp[std::max(0, std::min(i, h - 1)) * w + x];
            sumA += (p >> 24) & 0xFF; sumR += (p >> 16) & 0xFF; sumG += (p >> 8) & 0xFF; sumB += p & 0xFF;
        }
        for (int y = 0; y < h; y++) {
            pixels[y * w + x] = (int)((sumA / div) << 24) | (int)((sumR / div) << 16) | (int)((sumG / div) << 8) | (int)(sumB / div);
            int pOut = temp[std::max(0, y - r) * w + x]; int pIn = temp[std::min(h - 1, y + r + 1) * w + x];
            sumA += ((pIn >> 24) & 0xFF) - ((pOut >> 24) & 0xFF);
            sumR += ((pIn >> 16) & 0xFF) - ((pOut >> 16) & 0xFF);
            sumG += ((pIn >> 8) & 0xFF) - ((pOut >> 8) & 0xFF);
            sumB += (pIn & 0xFF) - (pOut & 0xFF);
        }
    }
    alignedFree(temp);
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurGaussian(
    JNIEnv* env, jclass clazz, jlong handle, jfloat radius) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
    float sigma = radius;
    float wIdeal = sqrt((12.0f * sigma * sigma / 3.0f) + 1.0f);
    int wl = (int)floor(wIdeal); if (wl % 2 == 0) wl--;
    int wu = wl + 2;
    float mIdeal = (12.0f * sigma * sigma - 3.0f * wl * wl - 12.0f * wl - 9.0f) / (-4.0f * wl - 4.0f);
    int m = (int)round(mIdeal);
    for (int i = 0; i < 3; i++) {
        Java_fastimage_FastImage_nativeBlurBox(env, clazz, handle, (float)(i < m ? wl : wu) / 2.0f);
    }
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurStack(
    JNIEnv* env, jclass, jlong handle, jfloat radius) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
    FastImage* img = (FastImage*)handle;
    int w = img->width; int h = img->height; int* pixels = img->pixels;
    int r = (int)(radius + 0.5f); if (r < 1) return; if (r > 254) r = 254;
    int div = (r + 1) * (r + 1);
    int* temp = alignedAlloc(w * h);
    for (int y = 0; y < h; y++) {
        int* srcRow = pixels + y * w; int* dstRow = temp + y * w;
        int rsum = 0, gsum = 0, bsum = 0, asum = 0;
        int rin = 0, gin = 0, bin = 0, ain = 0;
        int rout = 0, gout = 0, bout = 0, aout = 0;
        for (int i = -r; i <= 0; i++) {
            int p = srcRow[0]; int weight = i + r + 1;
            asum += ((p >> 24) & 0xFF) * weight; rsum += ((p >> 16) & 0xFF) * weight; gsum += ((p >> 8) & 0xFF) * weight; bsum += (p & 0xFF) * weight;
            ain += ((p >> 24) & 0xFF); rin += ((p >> 16) & 0xFF); gin += ((p >> 8) & 0xFF); bin += (p & 0xFF);
        }
        for (int i = 1; i <= r; i++) {
            int p = srcRow[std::min(i, w - 1)]; int weight = r + 1 - i;
            asum += ((p >> 24) & 0xFF) * weight; rsum += ((p >> 16) & 0xFF) * weight; gsum += ((p >> 8) & 0xFF) * weight; bsum += (p & 0xFF) * weight;
            aout += ((p >> 24) & 0xFF); rout += ((p >> 16) & 0xFF); gout += ((p >> 8) & 0xFF); bout += (p & 0xFF);
        }
        for (int x = 0; x < w; x++) {
            dstRow[x] = ((asum / div) << 24) | ((rsum / div) << 16) | ((gsum / div) << 8) | (bsum / div);
            asum -= ain; rsum -= rin; gsum -= gin; bsum -= bin;
            int pOut = srcRow[std::max(0, x - r)]; ain -= (pOut >> 24) & 0xFF; rin -= (pOut >> 16) & 0xFF; gin -= (pOut >> 8) & 0xFF; bin -= pOut & 0xFF;
            int pCenter = srcRow[std::min(x + 1, w - 1)]; aout += (pCenter >> 24) & 0xFF; rout += (pCenter >> 16) & 0xFF; gout += (pCenter >> 8) & 0xFF; bout += pCenter & 0xFF;
            ain += (pCenter >> 24) & 0xFF; rin += (pCenter >> 16) & 0xFF; gin += (pCenter >> 8) & 0xFF; bin += pCenter & 0xFF;
            int pIn = srcRow[std::min(x + r + 1, w - 1)]; aout -= (pIn >> 24) & 0xFF; rout -= (pIn >> 16) & 0xFF; gout -= (pIn >> 8) & 0xFF; bout -= pIn & 0xFF;
            asum += aout; rsum += rout; gsum += gout; bsum += bout;
        }
    }
    for (int x = 0; x < w; x++) {
        int rsum = 0, gsum = 0, bsum = 0, asum = 0;
        int rin = 0, gin = 0, bin = 0, ain = 0;
        int rout = 0, gout = 0, bout = 0, aout = 0;
        for (int i = -r; i <= 0; i++) {
            int p = temp[0 * w + x]; int weight = i + r + 1;
            asum += ((p >> 24) & 0xFF) * weight; rsum += ((p >> 16) & 0xFF) * weight; gsum += ((p >> 8) & 0xFF) * weight; bsum += (p & 0xFF) * weight;
            ain += ((p >> 24) & 0xFF); rin += ((p >> 16) & 0xFF); gin += ((p >> 8) & 0xFF); bin += (p & 0xFF);
        }
        for (int i = 1; i <= r; i++) {
            int p = temp[std::min(i, h - 1) * w + x]; int weight = r + 1 - i;
            asum += ((p >> 24) & 0xFF) * weight; rsum += ((p >> 16) & 0xFF) * weight; gsum += ((p >> 8) & 0xFF) * weight; bsum += (p & 0xFF) * weight;
            aout += ((p >> 24) & 0xFF); rout += ((p >> 16) & 0xFF); gout += ((p >> 8) & 0xFF); bout += (p & 0xFF);
        }
        for (int y = 0; y < h; y++) {
            pixels[y * w + x] = ((asum / div) << 24) | ((rsum / div) << 16) | ((gsum / div) << 8) | (bsum / div);
            asum -= ain; rsum -= rin; gsum -= gin; bsum -= bin;
            int pOut = temp[std::max(0, y - r) * w + x]; ain -= (pOut >> 24) & 0xFF; rin -= (pOut >> 16) & 0xFF; gin -= (pOut >> 8) & 0xFF; bin -= pOut & 0xFF;
            int pCenter = temp[std::min(y + 1, h - 1) * w + x]; aout += (pCenter >> 24) & 0xFF; rout += (pCenter >> 16) & 0xFF; gout += (pCenter >> 8) & 0xFF; bout += pCenter & 0xFF;
            ain += (pCenter >> 24) & 0xFF; rin += (pCenter >> 16) & 0xFF; gin += (pCenter >> 8) & 0xFF; bin += pCenter & 0xFF;
            int pIn = temp[std::min(y + r + 1, h - 1) * w + x]; aout -= (pIn >> 24) & 0xFF; rout -= (pIn >> 16) & 0xFF; gout -= (pIn >> 8) & 0xFF; bout -= pIn & 0xFF;
            asum += aout; rsum += rout; gsum += gout; bsum += bout;
        }
    }
    alignedFree(temp);
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurKawase(
    JNIEnv* env, jclass, jlong handle, jfloat radius, jint passes) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
    FastImage* img = (FastImage*)handle; int w = img->width; int h = img->height; int* pixels = img->pixels;
    if (passes < 1) passes = 1; if (passes > 5) passes = 5;
    int* temp = alignedAlloc(w * h); int* src = pixels; int* dst = temp;
    float offset = radius / (float)passes; if (offset < 1.0f) offset = 1.0f;
    for (int pass = 0; pass < passes; pass++) {
        int d = (int)(offset * (pass + 1)); if (d < 1) d = 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int x1 = std::max(0, x - d); int x2 = std::min(w - 1, x + d);
                int y1 = std::max(0, y - d); int y2 = std::min(h - 1, y + d);
                int p1 = src[y1 * w + x1]; int p2 = src[y1 * w + x2]; int p3 = src[y2 * w + x1]; int p4 = src[y2 * w + x2];
                int a = (((p1 >> 24) & 0xFF) + ((p2 >> 24) & 0xFF) + ((p3 >> 24) & 0xFF) + ((p4 >> 24) & 0xFF)) / 4;
                int r = (((p1 >> 16) & 0xFF) + ((p2 >> 16) & 0xFF) + ((p3 >> 16) & 0xFF) + ((p4 >> 16) & 0xFF)) / 4;
                int g = (((p1 >> 8) & 0xFF) + ((p2 >> 8) & 0xFF) + ((p3 >> 8) & 0xFF) + ((p4 >> 8) & 0xFF)) / 4;
                int b = ((p1 & 0xFF) + (p2 & 0xFF) + (p3 & 0xFF) + (p4 & 0xFF)) / 4;
                dst[y * w + x] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        int* t = src; src = dst; dst = t;
    }
    if (src != pixels) memcpy(pixels, src, w * h * sizeof(int));
    alignedFree(temp);
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurDualKawase(
    JNIEnv* env, jclass, jlong handle, jfloat radius) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
    Java_fastimage_FastImage_nativeBlurKawase(env, nullptr, handle, radius, 2);
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeBlurMipmapped(
    JNIEnv* env, jclass, jlong handle, jfloat radius) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
    FastImage* img = (FastImage*)handle; int w = img->width; int h = img->height; int* pixels = img->pixels;
    int levels = 0; float r = radius;
    while (r > 8.0f && levels < 3) { r /= 2.0f; levels++; }
    if (levels == 0) { Java_fastimage_FastImage_nativeBlurBox(env, nullptr, handle, radius); return; }
    int newW = w >> levels; int newH = h >> levels;
    if (newW < 4) newW = 4; if (newH < 4) newH = 4;
    int* mipPixels = alignedAlloc(newW * newH);
    for (int y = 0; y < newH; y++) {
        for (int x = 0; x < newW; x++) {
            int sumA = 0, sumR = 0, sumG = 0, sumB = 0, count = 0;
            for (int dy = 0; dy < (1 << levels); dy++) {
                for (int dx = 0; dx < (1 << levels); dx++) {
                    int sy = (y << levels) + dy; int sx = (x << levels) + dx;
                    if (sy < h && sx < w) {
                        int p = pixels[sy * w + sx];
                        sumA += (p >> 24) & 0xFF; sumR += (p >> 16) & 0xFF; sumG += (p >> 8) & 0xFF; sumB += p & 0xFF; count++;
                    }
                }
            }
            mipPixels[y * newW + x] = ((sumA / count) << 24) | ((sumR / count) << 16) | ((sumG / count) << 8) | (sumB / count);
        }
    }
    FastImage* mipImg = new FastImage(); mipImg->width = newW; mipImg->height = newH; mipImg->pixels = mipPixels; mipImg->owned = true;
    Java_fastimage_FastImage_nativeBlurBox(env, nullptr, (jlong)mipImg, r);
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            float fx = (float)(x * newW) / w; float fy = (float)(y * newH) / h;
            int ix = (int)fx; int iy = (int)fy; float dx = fx - ix; float dy = fy - iy;
            int x1 = std::min(ix, newW - 1); int x2 = std::min(ix + 1, newW - 1);
            int y1 = std::min(iy, newH - 1); int y2 = std::min(iy + 1, newH - 1);
            int p1 = mipPixels[y1 * newW + x1]; int p2 = mipPixels[y1 * newW + x2]; int p3 = mipPixels[y2 * newW + x1]; int p4 = mipPixels[y2 * newW + x2];
            int a = (int)((((p1 >> 24) & 0xFF) * (1-dx) * (1-dy) + ((p2 >> 24) & 0xFF) * dx * (1-dy) + ((p3 >> 24) & 0xFF) * (1-dx) * dy + ((p4 >> 24) & 0xFF) * dx * dy));
            int r = (int)((((p1 >> 16) & 0xFF) * (1-dx) * (1-dy) + ((p2 >> 16) & 0xFF) * dx * (1-dy) + ((p3 >> 16) & 0xFF) * (1-dx) * dy + ((p4 >> 16) & 0xFF) * dx * dy));
            int g = (int)((((p1 >> 8) & 0xFF) * (1-dx) * (1-dy) + ((p2 >> 8) & 0xFF) * dx * (1-dy) + ((p3 >> 8) & 0xFF) * (1-dx) * dy + ((p4 >> 8) & 0xFF) * dx * dy));
            int b = (int)(((p1 & 0xFF) * (1-dx) * (1-dy) + (p2 & 0xFF) * dx * (1-dy) + (p3 & 0xFF) * (1-dx) * dy + (p4 & 0xFF) * dx * dy));
            pixels[y * w + x] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
    alignedFree(mipPixels); delete mipImg;
}

JNIEXPORT void JNICALL Java_fastimage_FastImage_nativeGetPixels(
    JNIEnv* env, jclass, jlong handle, jintArray dstArray) {
    if (handle == 0) { throwFastImageException(env, "Native handle is null"); return; }
    FastImage* img = (FastImage*)handle;
    int count = img->width * img->height;
    jint* dst = env->GetIntArrayElements(dstArray, nullptr);
    memcpy(dst, img->pixels, count * sizeof(int));
    env->ReleaseIntArrayElements(dstArray, dst, 0);
}

} // extern "C"
