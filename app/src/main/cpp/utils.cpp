#include <jni.h>
#include <opencv2/core/base.hpp>
#include <android/bitmap.h>
#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc.hpp>

void bitmapToMat(JNIEnv *env, jobject bitmap, cv::Mat &dst) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    dst.create(info.height, info.width, CV_8UC4);
    cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
    tmp.copyTo(dst);
    AndroidBitmap_unlockPixels(env, bitmap);
    return;
}


void matToBitmap(JNIEnv *env, jclass, cv::Mat &src, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels = 0;

    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
    src.copyTo(tmp);
    AndroidBitmap_unlockPixels(env, bitmap);
    return;
}