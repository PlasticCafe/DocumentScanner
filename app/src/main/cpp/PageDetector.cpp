//
// Created by gamefreak on 12/13/2018.
//

#include "PageDetector.h"
#include <opencv2/imgproc.hpp>
#include <opencv2/opencv.hpp>

std::vector<Rectangle>
PageDetector::detect(std::vector<uint8_t> &frame, int32_t width, int32_t height, int32_t rotation) {
    cv::Mat mat = convert_frame(frame, width, height, rotation);
    cv::Rect2d result;
    tracker->update(mat, result);
    std::vector<Rectangle> list =  std::vector<Rectangle>();
    list.push_back(Rectangle(result.x, result.y, result.width, result.height));
    return list;
}

void PageDetector::initialize(std::vector<uint8_t> frame, cv::Rect2d roi, int32_t width,
                              int32_t height, int32_t rotation) {
    cv::Mat mat = convert_frame(frame, width, height, rotation);
    tracker->clear();
    tracker->init(mat, roi);
}

cv::Mat PageDetector::vecToMat(std::vector<uint8_t> &frame, int32_t width, int32_t height) {
   cv::Mat mat(height + height/2, width, CV_8UC1, frame.data());
   cv::cvtColor(mat, mat, cv::COLOR_YUV2RGBA_NV21);
   return mat;
}

cv::Mat PageDetector::convert_frame(std::vector<uint8_t> &frame, int32_t width, int32_t height, int32_t rotation) {
    cv::Mat mat = vecToMat(frame, width, height);
    rotate(mat, mat, rotation);
    return mat;

}

void PageDetector::rotate(cv::Mat input, cv::Mat output, int32_t rotation) {
    switch (rotation) {
        case (90):
            cv::rotate(input, output, cv::ROTATE_90_COUNTERCLOCKWISE);
            break;
        case (180):
            cv::rotate(input, output, cv::ROTATE_180);
            break;
        case (270):
            cv::rotate(input, output, cv::ROTATE_90_CLOCKWISE);
            break;
        default:
            break;
    }
}
