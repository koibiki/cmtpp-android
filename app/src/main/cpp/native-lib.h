#include <jni.h>
#include <string>
#include <opencv/cv.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <android/log.h>


#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "detect face", __VA_ARGS__)


extern "C" JNIEXPORT jstring
JNICALL Java_com_example_testcmt_MainActivity_stringFromJNI(JNIEnv *env, jobject);


extern "C" JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_rotateFrame(JNIEnv *env, jobject instance, jlong frame);

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_track(JNIEnv *env, jobject instance, jlong frame, jint left,
                                           jint top, jint width, jint height);

void orientationFrame(cv::Mat &src);

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_testBox(JNIEnv *env, jobject instance, jlong frame, jint left,
                                             jint top, jint width, jint height);