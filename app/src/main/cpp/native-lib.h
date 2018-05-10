#include <jni.h>
#include <string>
#include <opencv/cv.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>


#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "detect face", __VA_ARGS__)


extern "C" JNIEXPORT jstring
JNICALL Java_com_example_testcmt_MainActivity_stringFromJNI(JNIEnv *env, jobject);


extern "C" JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_orientationFrame(JNIEnv *env, jobject instance, jlong frame);


void orientationFrame(cv::Mat &src);