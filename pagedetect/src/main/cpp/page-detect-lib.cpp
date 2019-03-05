#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <opencv2/imgproc.hpp>
#include "PageDetector.h"
#include "PostProcess.h"

scanner::PageDetector *getPointer(JNIEnv *env, jobject instance) {
    jclass c = env->GetObjectClass(instance);
    jfieldID handle = env->GetFieldID(c, "mHandle", "J");
    return reinterpret_cast<scanner::PageDetector *>(env->GetLongField(instance, handle));
}

std::vector<cv::Point> listPointToVectorPoint(JNIEnv *env, jobject roi) {
    jclass listClass = env->FindClass("java/util/ArrayList");
    jclass pointClass = env->FindClass("cafe/plastic/pagedetect/Vec2");
    jmethodID getSize = env->GetMethodID(listClass, "size", "()I");
    jmethodID get = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");
    jfieldID fX = env->GetFieldID(pointClass, "x", "F");
    jfieldID fY = env->GetFieldID(pointClass, "y", "F");
    jint size = env->CallIntMethod(roi, getSize);
    std::vector<cv::Point> vecRoi;
    for (jint i = 0; i < size; i++) {
        jobject point = env->CallObjectMethod(roi, get, i);
        int x = (int) env->GetFloatField(point, fX);
        int y = (int) env->GetFloatField(point, fY);
        vecRoi.push_back(cv::Point(x, y));
    }
    return vecRoi;
}


jobject resultsToArrayList(JNIEnv *env, std::vector<cv::Point> roi) {
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jclass pointClass = env->FindClass("cafe/plastic/pagedetect/Vec2");

    jobject arrayList = env->NewObject(arrayListClass,
                                       env->GetMethodID(arrayListClass, "<init>", "()V"));
    for (int i = 0; i < roi.size(); i++) {
        jobject point = env->NewObject(pointClass, env->GetMethodID(pointClass, "<init>", "(FF)V"),
                                       (jfloat) roi[i].x,
                                       (jfloat) roi[i].y);
        env->CallBooleanMethod(arrayList,
                               env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z"),
                               point);
    }
    return arrayList;
}

cv::Mat lockBitmap(JNIEnv *env,
                   jobject bitmap) { //unlockBitmap MUST be called on the bitmap before returning from native code
    AndroidBitmapInfo info;
    void *pixels = 0;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    return cv::Mat(info.height, info.width, CV_8UC4, pixels);
}

void unlockBitmap(JNIEnv *env, jobject bitmap) {
    AndroidBitmap_unlockPixels(env, bitmap);
    return;
}

extern "C" {
JNIEXPORT jlong JNICALL
Java_cafe_plastic_pagedetect_PageDetector_Create(JNIEnv *env, jobject instance) {
    return (jlong) (new scanner::PageDetector());
}

JNIEXPORT jobject JNICALL
Java_cafe_plastic_pagedetect_PageDetector_GetRoi(JNIEnv *env, jobject instance,
                                                 jbyteArray frame_, jint width,
                                                 jint height) {
    jbyte *frame = env->GetByteArrayElements(frame_, NULL);
    jsize size = env->GetArrayLength(frame_);
    std::vector<uint8_t> vecFrame((uint8_t *) frame, (uint8_t *) (frame + size));
    scanner::PageDetector *pageDetector = getPointer(env, instance);
    return resultsToArrayList(env, pageDetector->detect_nv21(vecFrame, width, height));
}

JNIEXPORT void JNICALL
Java_cafe_plastic_pagedetect_PageDetector_Release(JNIEnv *env, jobject instance) {
    delete getPointer(env, instance);
}

JNIEXPORT jfloat JNICALL
Java_cafe_plastic_pagedetect_PageDetector_GetArea(JNIEnv *env, jobject instance,
                                                  jobject roi) {
    std::vector<cv::Point> vRoi = listPointToVectorPoint(env, roi);
    scanner::PageDetector *pageDetector = getPointer(env, instance);
    return pageDetector->getArea(vRoi);

}

JNIEXPORT jfloat JNICALL
Java_cafe_plastic_pagedetect_PageDetector_GetDistortion(JNIEnv *env, jobject instance,
                                                        jobject roi) {
    std::vector<cv::Point> vRoi = listPointToVectorPoint(env, roi);
    scanner::PageDetector *pageDetector = getPointer(env, instance);
    return static_cast<jfloat>(pageDetector->distortion(vRoi));
}

}extern "C"
JNIEXPORT void JNICALL
Java_cafe_plastic_pagedetect_PostProcess_Brightness(JNIEnv *env, jobject instance, jobject input,
                                                    jobject output, jfloat brightness,
                                                    jfloat contrast, jboolean same) {
    cv::Mat inputMat = lockBitmap(env, input);
    cv::Mat outputMat;
    if (same)
        outputMat = inputMat;
    else
        outputMat = lockBitmap(env, output);
    scanner::brightnessAndContrast(inputMat, outputMat, brightness, contrast);
    unlockBitmap(env, input);
    if (!same)
        unlockBitmap(env, output);
}
extern "C"
JNIEXPORT void JNICALL
Java_cafe_plastic_pagedetect_PostProcess_Threshold(JNIEnv *env, jclass type, jobject input,
                                                   jobject output) {

    cv::Mat inputMat = lockBitmap(env, input);
    cv::Mat outputMat = lockBitmap(env, output);
    scanner::threshold(inputMat, outputMat);
    unlockBitmap(env, input);
    unlockBitmap(env, output);
}