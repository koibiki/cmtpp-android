#include "native-lib.h"

bool isInit = false;
CMT *cmt_p;

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
    orientationFrame(*src);
    LOGW("rows = %d  cols = %d", src->rows, src->cols);
    if (!isInit) {
        isInit = true;
        CMT* cmt = new CMT();
        cmt_p =  cmt;
        Rect rect = CvRect(left, top, width, height);
        cv::rectangle(*src, rect, Scalar(255, 255, 255), 1, 1, 0);
        Mat im0_gray;
        cvtColor(*src, im0_gray, CV_BGR2GRAY);
        cmt->initialize(im0_gray, rect);
        return (jlong) src;
    } else {
        Mat im_gray;
        if (src->channels() > 1) {
            cvtColor(*src, im_gray, CV_BGR2GRAY);
        }
        cmt_p->processFrame(im_gray);
//        display(*src, *cmt_p);
        Point2f vertices[4];
        cmt_p->bb_rot.points(vertices);
        for (int i = 0; i < 4; i++) {
            line(*src, vertices[i], vertices[(i + 1) % 4], Scalar(255, 0, 0));
        }
        return (jlong) src;
    }
}

void orientationFrame(Mat &src) {
    Mat dst;
    transpose(src, dst);
    flip(dst, src, 0);
}

Mat display(Mat &im, CMT &cmt) {
//    for (size_t i = 0; i < cmt.points_active.size(); i=i+10) {
//        circle(im, cmt.points_active[i], 2, Scalar(255, 0, 0));
//    }
    Point2f vertices[4];
    cmt.bb_rot.points(vertices);
    for (int i = 0; i < 4; i++) {
        line(im, vertices[i], vertices[(i + 1) % 4], Scalar(255, 0, 0));
    }
}