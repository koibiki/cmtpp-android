#include "native-lib.h"

bool isInit = false;
CMT *cmt_p;
float scale = 0.25;

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
    cv::Mat *src = (cv::Mat *) frame;
    orientationFrame(*src);
    if (!isInit) {
        isInit = true;
        CMT *cmt = new CMT();
        cmt_p = cmt;
        Rect rect = CvRect(left * scale, top * scale, width * scale, height * scale);
        cv::rectangle(*src, rect, Scalar(255, 255, 255), 1, 1, 0);
        Mat im0_gray;
        cvtColor(*src, im0_gray, CV_BGR2GRAY);
        cv::Size dsize = cv::Size(im0_gray.cols * scale, im0_gray.rows * scale);
        Mat img_resize = Mat(dsize, CV_32S);
        resize(im0_gray, img_resize, dsize);
        cmt->initialize(img_resize, rect);
        return (jlong) src;
    } else {
        Mat im_gray;
        cvtColor(*src, im_gray, CV_BGR2GRAY);
        cv::Size dsize = cv::Size(im_gray.cols * scale, im_gray.rows * scale);
        Mat img_resize = Mat(dsize, CV_32S);
        resize(im_gray, img_resize, dsize);
        cmt_p->processFrame(img_resize);
        Point2f vertices[4];
        cmt_p->bb_rot.points(vertices);
        for (int i = 0; i < 4; i++) {
            vertices[i].x = vertices[i].x / scale;
            vertices[i].y = vertices[i].y / scale;
        }
        for (int i = 0; i < 4; i++) {
            line(*src, vertices[i], vertices[(i + 1) % 4], Scalar(255, 0, 0));
        }
        return (jlong) src;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_testcmt_CmtActivity_clear(JNIEnv *env, jobject instance) {
    delete cmt_p;
}

void orientationFrame(Mat &src) {
    Mat dst;
    transpose(src, dst);
    flip(dst, src, -1);
}

Mat display(Mat &im, CMT &cmt) {
    Point2f vertices[4];
    cmt.bb_rot.points(vertices);
    for (int i = 0; i < 4; i++) {
        line(im, vertices[i], vertices[(i + 1) % 4], Scalar(255, 0, 0));
    }
}