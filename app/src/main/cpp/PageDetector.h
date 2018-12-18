//
// Created by gamefreak on 12/13/2018.
//

#ifndef DOCUMENTSCANNER_PAGEDETECTOR_H
#define DOCUMENTSCANNER_PAGEDETECTOR_H

#include <vector>
#include <opencv2/core/types.hpp>
#include <opencv2/core/mat.hpp>
#include <opencv2/tracking/tracker.hpp>
#include "Rectangle.h"

class PageDetector {
public:
    PageDetector();
    std::vector<Rectangle> detect(std::vector<uint8_t> &frame, int32_t width, int32_t height, int32_t rotation);
    void initialize(std::vector<uint8_t> frame, cv::Rect2d roi, int32_t width, int32_t height, int32_t rotation);
private:
    cv::Ptr<cv::Tracker> tracker = cv::TrackerMOSSE::create();
    cv::Mat vecToMat(std::vector<uint8_t> &frame, int32_t width, int32_t height);
    void rotate(cv::Mat &input, cv::Mat &output, int32_t rotation);
    cv::Mat convert_frame(std::vector<uint8_t> &frame, int32_t width, int32_t height, int32_t rotation);
    Rectangle currentRoi = Rectangle(0, 0, 1, 1);
};

#endif //DOCUMENTSCANNER_PAGEDETECTOR_H
