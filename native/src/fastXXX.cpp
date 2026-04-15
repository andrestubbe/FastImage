#include "fastXXX.h"
#include <windows.h>
#include <stdio.h>

// Example JNI implementation
JNIEXPORT jstring JNICALL Java_fastXXX_FastXXX_hello(JNIEnv* env, jobject obj) {
    printf("[DEBUG C++] hello() called\n");
    fflush(stdout);
    return env->NewStringUTF("Hello from FastXXX native code!");
}
