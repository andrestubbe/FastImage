#ifndef FASTXXX_H
#define FASTXXX_H

#include <jni.h>
#include <windows.h>

#ifdef __cplusplus
extern "C" {
#endif

// Example JNI export
JNIEXPORT jstring JNICALL Java_fastXXX_FastXXX_hello(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif

#endif // FASTXXX_H
