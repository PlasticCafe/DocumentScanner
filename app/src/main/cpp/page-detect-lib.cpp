#include <jni.h>
#include <string>
#include "Rectangle.h"
#include "PageDetector.h"

PageDetector *getPointer(JNIEnv *env, jobject instance) {
    jclass c = env->GetObjectClass(instance);
    jfieldID handle = env->GetFieldID(c, "mHandle", "J");
    return reinterpret_cast<PageDetector *>(env->GetLongField(instance, handle));
}

std::vector<cv::Point> listPointToVectorPoint(JNIEnv *env, jobject roi) {
    jclass listClass = env->FindClass("java/util/List");
    jclass pointClass = env->FindClass("android/graphics/Point");
    jmethodID getSize = env->GetMethodID(listClass, "size", "()I");
    jmethodID get = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");
    jfieldID fX = env->GetFieldID(pointClass, "x", "I");
    jfieldID fY = env->GetFieldID(pointClass, "y", "I");
    jint size = env->CallIntMethod(roi, getSize);
    std::vector<cv::Point> vecRoi;
    for (jint i = 0; i < size; i++) {
        jobject point = env->CallObjectMethod(roi, get, i);
        int x = env->GetIntField(point, fX);
        int y = env->GetIntField(point, fY);
        vecRoi.push_back(cv::Point(x, y));
    }
    return vecRoi;
}


jobject resultsToArrayList(JNIEnv *env, std::vector<cv::Point> roi) {
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jclass pointClass = env->FindClass("android/graphics/Point");

    jobject arrayList = env->NewObject(arrayListClass,
                                       env->GetMethodID(arrayListClass, "<init>", "()V"));
    for (int i = 0; i < roi.size(); i++) {
        jobject point = env->NewObject(pointClass, env->GetMethodID(pointClass, "<init>", "(II)V"),
                                       roi[i].x,
                                       roi[i].y);
        env->CallBooleanMethod(arrayList,
                               env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z"),
                               point);
    }
    return arrayList;
}

extern "C" {
JNIEXPORT jlong JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_Create(JNIEnv *env, jobject instance) {
    return (jlong) (new PageDetector());
}

JNIEXPORT jobject JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_GetRoi(JNIEnv *env, jobject instance,
                                                             jbyteArray frame_, jint width,
                                                             jint height, jint rotation) {
    jbyte *frame = env->GetByteArrayElements(frame_, NULL);
    jsize size = env->GetArrayLength(frame_);
    std::vector<uint8_t> vecFrame((uint8_t *) frame, (uint8_t *) (frame + size));
    PageDetector *pageDetector = getPointer(env, instance);
    return resultsToArrayList(env, pageDetector->detect_nv21(vecFrame, width, height, rotation));
}

JNIEXPORT void JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_Release(JNIEnv *env, jobject instance) {
    delete getPointer(env, instance);
}

JNIEXPORT jfloat JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_GetArea(JNIEnv *env, jobject instance,
                                                              jobject roi) {
    std::vector<cv::Point> vRoi = listPointToVectorPoint(env, roi);
    PageDetector *pageDetector = getPointer(env, instance);
    return pageDetector->getArea(vRoi);

}

JNIEXPORT jfloat JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_GetDistortion(JNIEnv *env, jobject instance,
                                                                    jobject roi) {
    std::vector<cv::Point> vRoi = listPointToVectorPoint(env, roi);
    PageDetector *pageDetector = getPointer(env, instance);
    return pageDetector->distortion(vRoi);
}
}