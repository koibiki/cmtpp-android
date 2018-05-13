#include <jni.h>
#include "CMT.h"
#include <string>
#include <opencv/cv.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <android/log.h>


#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "native-lib", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "native-lib", __VA_ARGS__)


using cv::Rect;
using cv::Scalar;
using cmt::CMT;

extern "C" JNIEXPORT jstring
JNICALL Java_com_example_testcmt_MainActivity_stringFromJNI(JNIEnv *env, jobject);


extern "C" JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_rotateFrame(JNIEnv *env, jobject instance, jlong frame);

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_testcmt_CmtActivity_track(JNIEnv *env, jobject instance, jlong frame, jint left,
                                           jint top, jint width, jint height);

void orientationFrame(cv::Mat &src);
Mat display(Mat &im, CMT &cmt);

extern "C"
JNIEXPORT void JNICALL
Java_com_example_testcmt_CmtActivity_clear(JNIEnv *env, jobject instance);