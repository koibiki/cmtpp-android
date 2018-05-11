#include "native-lib.h"

using cv::Rect;
using cv::Scalar;

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_example_testcmt_MainActivity_stringFromJNI(JNIEnv *env, jobject) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_rotateFrame(JNIEnv *env, jobject instance, jlong m_addr) {
    cv::Mat *src = (cv::Mat *) m_addr;
    LOGW("src = %ld  m_addr = %ld", (long) src, (long) m_addr);
    orientationFrame(*src);
    return (jlong) src;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_track(JNIEnv *env, jobject instance, jlong frame, jint left,
                                           jint top, jint width, jint height) {

}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_testBox(JNIEnv *env, jobject instance, jlong frame, jint left,
                                             jint top, jint width, jint height) {
    cv::Mat *src = (cv::Mat *) frame;
    LOGW("src = %ld  m_addr = %ld", (long) src, (long) frame);
    orientationFrame(*src);
    Rect rect = Rect(left, top, width, height);
    cvRectangle(*src, cvPoint(100, 100), cvPoint(200, 200), cvScalar(0, 0, 255), 3, 4, 0 );
    return (jlong) src;


}

void orientationFrame(cv::Mat &src) {
    cv::Mat dst;
    transpose(src, dst);
    cv::flip(dst, src, 0);
}