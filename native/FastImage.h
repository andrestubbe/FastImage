/**
 * FastImage - Native Image Processing Header
 */

#ifndef FASTIMAGE_H
#define FASTIMAGE_H

#include <jni.h>

// Image structure
struct FastImage {
    int width;
    int height;
    int* pixels;
    bool owned;
};

// Alignment helpers
int* alignedAlloc(int count);
void alignedFree(int* ptr);

#endif // FASTIMAGE_H
