/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <JNI_X264Encoder.h>
#include <X264Encoder.h>

static X264Encoder *encoder;
#ifdef __cplusplus
extern "C" {
#endif

static bool encode(jbyte *src, jbyte *dest, int *size, int *type) {
    bool result = encoder->encode((char *) src, (char *) dest, size, type);
    return result;
}

void Java_com_lmy_codec_x264_X264Encoder_init
        (JNIEnv *env, jobject thiz) {
    encoder = new X264Encoder();
}

void Java_com_lmy_codec_x264_X264Encoder_start
        (JNIEnv *env, jobject thiz) {
    if (!encoder->start()) {
        LOGE("X264Encoder start failed!");
    }
}

void Java_com_lmy_codec_x264_X264Encoder_stop
        (JNIEnv *env, jobject thiz) {
    encoder->stop();
    delete encoder;
    encoder = NULL;
}

jboolean Java_com_lmy_codec_x264_X264Encoder_encode
        (JNIEnv *env, jobject thiz, jbyteArray src, jbyteArray dest, jintArray size,
         jintArray type) {
    jbyte *srcBuffer = env->GetByteArrayElements(src, JNI_FALSE);
    jbyte *destBuffer = env->GetByteArrayElements(dest, JNI_FALSE);
    jint *pSize = env->GetIntArrayElements(size, JNI_FALSE);
    jint *pType = env->GetIntArrayElements(type, JNI_FALSE);
    bool result = encode(srcBuffer, destBuffer, pSize, pType);
    env->ReleaseByteArrayElements(src, srcBuffer, JNI_FALSE);
    env->ReleaseByteArrayElements(dest, destBuffer, JNI_FALSE);
    env->ReleaseIntArrayElements(size, pSize, JNI_FALSE);
    env->ReleaseIntArrayElements(type, pType, JNI_FALSE);
    return (jboolean) result;
}

void Java_com_lmy_codec_x264_X264Encoder_setVideoSize
        (JNIEnv *env, jobject thiz, jint width, jint height) {
    encoder->setVideoSize(width, height);
}

void Java_com_lmy_codec_x264_X264Encoder_setBitrate
        (JNIEnv *env, jobject thiz, jint bitrate) {
    encoder->setBitrate(bitrate);
}

void Java_com_lmy_codec_x264_X264Encoder_setFrameFormat
        (JNIEnv *env, jobject thiz, jint format) {
    encoder->setFrameFormat(format);
}

void Java_com_lmy_codec_x264_X264Encoder_setFps
        (JNIEnv *env, jobject thiz, jint fps) {
    encoder->setFps(fps);
}

void Java_com_lmy_codec_x264_X264Encoder_setProfile
        (JNIEnv *env, jobject thiz, jstring profile) {
    char *profileTmp = (char *) env->GetStringUTFChars(profile, NULL);
    encoder->setProfile(profileTmp);
    env->ReleaseStringUTFChars(profile, profileTmp);
}

void Java_com_lmy_codec_x264_X264Encoder_setLevel
        (JNIEnv *env, jobject thiz, jint level) {
    encoder->setLevel(level);
}

static const char *classPathName = "com/lmy/codec/x264/X264Encoder";

static JNINativeMethod methods[] = {
        {"init",           "()V",                   (void *) Java_com_lmy_codec_x264_X264Encoder_init},
        {"start",          "()V",                   (void *) Java_com_lmy_codec_x264_X264Encoder_start},
        {"stop",           "()V",                   (void *) Java_com_lmy_codec_x264_X264Encoder_stop},
        {"encode",         "([B[B[I[I)Z",           (void *) Java_com_lmy_codec_x264_X264Encoder_encode},
        {"setVideoSize",   "(II)V",                 (void *) Java_com_lmy_codec_x264_X264Encoder_setVideoSize},
        {"setBitrate",     "(I)V",                  (void *) Java_com_lmy_codec_x264_X264Encoder_setBitrate},
        {"setFrameFormat", "(I)V",                  (void *) Java_com_lmy_codec_x264_X264Encoder_setFrameFormat},
        {"setFps",         "(I)V",                  (void *) Java_com_lmy_codec_x264_X264Encoder_setFps},
        {"setProfile",     "(Ljava/lang/String;)V", (void *) Java_com_lmy_codec_x264_X264Encoder_setProfile},
        {"setLevel",       "(I)V",                  (void *) Java_com_lmy_codec_x264_X264Encoder_setLevel},
};

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, classPathName,
                               methods, sizeof(methods) / sizeof(methods[0]))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

typedef union {
    JNIEnv *env;
    void *venv;
} UnionJNIEnvToVoid;

/* This function will be call when the library first be loaded */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    UnionJNIEnvToVoid uenv;
    JNIEnv *env = NULL;
    //LOGI("JNI_OnLoad!");

    if (vm->GetEnv((void **) &uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        //LOGE("ERROR: GetEnv failed");
        return -1;
    }

    env = uenv.env;;

    //jniRegisterNativeMethods(env, "whf/jnitest/Person", methods, sizeof(methods) / sizeof(methods[0]));

    if (registerNatives(env) != JNI_TRUE) {
        //LOGE("ERROR: registerNatives failed");
        return -1;
    }

    return JNI_VERSION_1_4;
}

#ifdef __cplusplus
}
#endif