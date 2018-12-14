#include <jni.h>
#include <string>
#include "PageDetector.h"

extern "C"
PageDetector *getPointer(JNIEnv *env, jobject instance) {
    jclass c = env->GetObjectClass(instance);
    jfieldID handle = env->GetFieldID(c, "mHandle", "J");
    return reinterpret_cast<PageDetector *>(env->GetLongField(instance, handle));
}

extern "C"
jobject resultsToArrayList(JNIEnv *env, std::vector<Rectangle> vecRectangle) {
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jclass rectClass = env->FindClass("android/graphics/Rect");

    jobject arrayList = env->NewObject(arrayListClass,
                                       env->GetMethodID(arrayListClass, "<init>", "()V"));
    for (int i = 0; i < vecRectangle.size(); i++) {
        jobject rect = env->NewObject(rectClass, env->GetMethodID(rectClass, "<init>", "(IIII)V"),
                                      vecRectangle[i].getX(),
                                      vecRectangle[i].getY(),
                                      vecRectangle[i].getX2(),
                                      vecRectangle[i].getY2());
        env->CallVoidMethod(arrayList, env->GetMethodID(arrayListClass, "add", "(java/lang/Object)V"), rect);
    }
    return arrayList;
}

extern "C" JNIEXPORT jstring JNICALL
Java_cafe_plastic_documentscanner_NavigatorActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cafe_plastic_documentscanner_ui_fragments_CaptureFragment_stringFromJNI(JNIEnv *env,
                                                                             jobject instance) {

    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());

}

extern "C"
JNIEXPORT jlong JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_Create(JNIEnv *env, jobject instance) {
    return (jlong) (new PageDetector());
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_Initialize(JNIEnv *env, jobject instance,
                                                                 jbyteArray frame_, jint width,
                                                                 jint height,
                                                                 jint rotation, jint left, jint top,
                                                                 jint right, jint bottom) {
    jbyte *frame = env->GetByteArrayElements(frame_, NULL);
    jsize size = env->GetArrayLength(frame_);
    std::vector<uint8_t> vecFrame((uint8_t) frame, (uint8_t) (frame + size));
    cv::Rect2d roi = cv::Rect2d(left, top, right - left, bottom - top);
    PageDetector *pageDetector = getPointer(env, instance);
    pageDetector->initialize(vecFrame, roi, width, height, rotation);
    env->ReleaseByteArrayElements(frame_, frame, 0);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_Detect(JNIEnv *env, jobject instance,
                                                             jbyteArray frame_, jint width,
                                                             jint height, jint rotation) {
    jbyte *frame = env->GetByteArrayElements(frame_, NULL);
    jsize size = env->GetArrayLength(frame_);
    std::vector<uint8_t> vecFrame((uint8_t) frame, (uint8_t) (frame + size));
    PageDetector *pageDetector = getPointer(env, instance);
    std::vector<Rectangle> results = pageDetector->detect(vecFrame, width, height, rotation);
    env->ReleaseByteArrayElements(frame_, frame, 0);
    return resultsToArrayList(env, results);
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_plastic_documentscanner_vision_PageDetector_Release(JNIEnv *env, jobject instance) {
    delete getPointer(env, instance);
}
