//
// Created by nonolive66 on 2018/4/25.
//
#include <com_lmy_codec_helper_GLHelper.h>

//Log
#ifdef ANDROID

#include <jni.h>
#include <GLES2/gl2.h>

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "JNI", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "JNI", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#endif

JNIEXPORT void JNICALL Java_com_lmy_codec_helper_GLHelper_glReadPixels
        (JNIEnv *env, jobject thiz, jint x, jint y, jint width, jint height, jint format,
         jint type) {
    glReadPixels(x, y, width, height, format, type, 0);
}
