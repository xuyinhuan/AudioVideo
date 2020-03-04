#include <jni.h>
#include <string>

extern "C" {
#include <libavcodec/avcodec.h>
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_yinhuanxu_audiovideo_FFmpegActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++ ";
    std::string ffmpegConfig = avcodec_configuration();
    return env->NewStringUTF(hello.append(ffmpegConfig).c_str());
}
