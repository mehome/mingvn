#include <jni.h>
#include <string>

extern "C"
jstring
Java_vxgstreamcontrol_huawei_com_vxgstreamcontrol_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
