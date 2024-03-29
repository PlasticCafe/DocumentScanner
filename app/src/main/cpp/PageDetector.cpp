
//
// Created by gamefreak on 12/13/2018.
//
#include "PageDetector.h"
#include <opencv2/imgproc.hpp>
#include <opencv2/opencv.hpp>

#ifdef __ANDROID_NDK__

#include <android/bitmap.h>

#endif

PageDetector::PageDetector(float scale) {
    this->scale = scale;
}

std::vector<cv::Point>
PageDetector::detect_nv21(std::vector<uint8_t> &frameInput, int32_t width, int32_t height) {
    assert(("The input frame is too small", width > 4 || height > 4));
    assert(("Input frame is not in NV21 format", frameInput.size() ==
                                                 (height + height / 2) * width));
    cv::Mat frame = getMat_nv21(frameInput, width, height);
    return this->detect(frame);
}

std::vector<cv::Point> PageDetector::detect(cv::Mat &frameInput) {
    int width = frameInput.cols;
    int height = frameInput.rows;
    if (width * height > 800 * 600) {
        cv::resize(frameInput, frameInput, cv::Size(), scale, scale);
    } else {
        this->scale = 1;
    }
    //rotate(frameInput, frameInput, rotation);
    edgeDetect(frameInput);
#ifndef __ANDROID_NDK__
    cv::imshow("edge", frameInput);
#endif
    std::vector<cv::Point> detectedRoi = findPage(frameInput);
    return detectedRoi;
}

cv::Mat PageDetector::getMat_nv21(std::vector<uint8_t> &frame, int32_t width, int32_t height) {
    cv::Mat mat(height + height / 2, width, CV_8UC1, frame.data());
    cv::cvtColor(mat, mat, cv::COLOR_YUV2GRAY_NV21);
    return mat;
}


cv::Mat PageDetector::getFrameSection(cv::Mat &frameInput, std::vector<cv::Point> roi) {
    assert(("Wrong number of points, quad expected", roi.size() == 4));
    int width = (int) std::min(cv::norm(roi[0] - roi[1]), cv::norm(roi[3] - roi[2]));
    int height = (int) std::min(cv::norm(roi[0] - roi[3]), cv::norm(roi[1] - roi[2]));
    assert(("Preview dimensions exceed frame size",
            width <= frameInput.cols && height <= frameInput.rows));
    cv::Point2f src[4];
    for (int i = 0; i < 4; i++) src[i] = roi[i];
    cv::Point2f dst[4];
    dst[0] = cv::Point2f(0, 0);
    dst[1] = cv::Point2f(width, 0);
    dst[2] = cv::Point2f(width, height);
    dst[3] = cv::Point2f(0, height);
    cv::Mat warpMatrix = cv::getPerspectiveTransform(src, dst);
    cv::Mat output;
    cv::warpPerspective(frameInput, output, warpMatrix, cv::Size(width, height));
    return output;
}


cv::Mat PageDetector::edgeDetect(cv::Mat &input) {

    int frameScale = getMatScale(input);
    cv::medianBlur(input, input, frameScale / 50 | 1);
    double threshold = cv::threshold(input, input.clone(), 0, 255,
                                     cv::THRESH_BINARY | cv::THRESH_OTSU);
    cv::Canny(input, input, threshold * 0.3, threshold);
    input.rowRange(0, 1).setTo(cv::Scalar(0));
    input.rowRange(input.rows - 2, input.rows - 1).setTo(cv::Scalar(0));
    cv::dilate(input, input, cv::Mat(), cv::Point(-1, -1), frameScale / 85);
    cv::erode(input, input, cv::Mat(), cv::Point(-1, -1), (frameScale / 85) - 1);
    return input;
}

float PageDetector::getArea(std::vector<cv::Point> &points) {
    int acc = 0;
    for (int i = 0; i < points.size(); i++) {
        cv::Point &p1 = points[i];
        cv::Point &p2 = points[(i + 1) % points.size()];
        acc += (p2.x - p1.x) * (p2.y + p1.y);
    }
    return (acc * -0.5f);
}

void PageDetector::makePointsClockwise(std::vector<cv::Point> &points) {
    if (getArea(points) >= 0) {
        return;
    } else {
        std::reverse(points.begin(), points.end());
        return;
    }
}

double
PageDetector::distortion(std::vector<cv::Point> &points) { //average angular distortion in degrees
    double acc = 0;
    for (int i = 0; i < points.size(); i++) {
        cv::Point2f vec1 = points[(i + 1) % points.size()] - points[i];
        cv::Point2f vec2 = points[(i - 1) % points.size()] - points[i];
        vec1 = vec1 / cv::norm(vec1);
        vec2 = vec2 / cv::norm(vec2);
        float distortion = cv::abs(vec1.dot(vec2));
        if (distortion > acc) {
            acc = distortion;
        }
    }
    acc = 1.0f - acc;
    return acos(acc) * 180.0 / CV_PI;
}

void PageDetector::alignTopEdge(std::vector<cv::Point> &points) {
    int topPointIdx = 0;
    for (int i = 0; i < points.size(); i++) {
        if (points[topPointIdx].y > points[i].y) {
            topPointIdx = i;
        }
    }
    int prevIdx = static_cast<int>((topPointIdx - 1) % points.size());
    cv::Point vec1 = points[prevIdx] - points[topPointIdx];
    cv::Point vec2 = points[topPointIdx] - points[(topPointIdx + 1) % points.size()];
    vec1 = vec1 / cv::norm(vec1);
    vec2 = vec2 / cv::norm(vec2);
    cv::Point topEdge = cv::Point(0, 1);
    double angle1 = cv::abs(topEdge.dot(vec1));
    double angle2 = cv::abs(topEdge.dot(vec2));
    if (angle1 < angle2) {
        std::rotate(points.begin(), points.begin() + prevIdx, points.end());
    } else {
        std::rotate(points.begin(), points.begin() + topPointIdx, points.end());
    }
}

int PageDetector::getMatScale(cv::Mat &frameInput) {
    return cvCeil(cv::sqrt(frameInput.rows * frameInput.cols)) | 1;
}

std::vector<cv::Point> PageDetector::findPage(cv::Mat &edges) {
    std::vector<std::vector<cv::Point> > contours;
    std::vector<cv::Vec4i> hierarchy;
    std::vector<cv::Point> approx;
    std::vector<cv::Point> largestContour;
    double largestArea = 0;
    double minArea = edges.cols * edges.rows * 0.10;
    cv::findContours(edges, contours, hierarchy, cv::RETR_LIST, cv::CHAIN_APPROX_NONE);
    for (int i = 0; i < contours.size(); i++) {
        std::vector<cv::Point> contour = contours[i];
        double arcLength = cv::arcLength(contour, true);
        cv::approxPolyDP(contour, approx, arcLength * 0.05, true);
        if (approx.size() == 4) {
            double area = cv::contourArea(approx);
            makePointsClockwise(approx);
            if (area > largestArea && area > minArea && distortion(approx) <= 45.0) {
                alignTopEdge(approx);
                largestContour = approx;
                largestArea = area;
            }

        }
    }
    for (int i = 0; i < largestContour.size(); i++) {
        largestContour[i].x /= scale;
        largestContour[i].y /= scale;
    }
    return largestContour;
}

void PageDetector::threshold(cv::Mat &frameInput) {
    int frameScale = getMatScale(frameInput);
    cv::Mat tmp;
    cv::cvtColor(frameInput, tmp, cv::COLOR_BGR2GRAY);
    cv::GaussianBlur(tmp, tmp, cv::Size(frameScale / 200 | 1, frameScale / 200 | 1), 0);
    cv::adaptiveThreshold(tmp, tmp, 255, cv::ADAPTIVE_THRESH_MEAN_C, cv::THRESH_BINARY,
                          frameScale / 238 | 1, 2);
    //cv::Mat element = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(3, 3), cv::Point(-1, -1));
    //cv::morphologyEx(tmp, tmp, cv::MORPH_CLOSE, element);
    cv::cvtColor(tmp, frameInput, cv::COLOR_GRAY2BGR);
}


#ifdef __ANDROID_NDK__

void PageDetector::bitmapToMat(JNIEnv *env, jobject bitmap, cv::Mat &dst) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    dst.create(info.height, info.width, CV_8UC4);
    cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
    cv::cvtColor(tmp, dst, cv::COLOR_BGR5652BGR);
    AndroidBitmap_unlockPixels(env, bitmap);
    return;
}

void PageDetector::matToBitmap(JNIEnv *env, cv::Mat &src, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
    cv::cvtColor(src, tmp, cv::COLOR_BGR2BGR565);
    AndroidBitmap_unlockPixels(env, bitmap);
    return;
}


#endif







