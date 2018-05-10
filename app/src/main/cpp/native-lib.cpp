#include "native-lib.h"

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_example_testcmt_MainActivity_stringFromJNI(JNIEnv *env, jobject) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_orientationFrame(JNIEnv *env, jobject instance, jlong m_addr) {

    cv::Mat *src = (cv::Mat *) m_addr;
    LOGW("src = %ld  m_addr = %ld", (long) src, (long) m_addr);
    orientationFrame(*src);
    return (jlong) src;

}


void orientationFrame(cv::Mat &src) {
    cv::Mat dst;
    transpose(src, dst);
    cv::flip(dst, src, 0);
}