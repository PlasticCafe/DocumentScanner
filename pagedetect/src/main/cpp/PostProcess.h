//
// Created by gamefreak on 2/21/19.
//

#ifndef DOCUMENTSCANNER_POSTPROCESS_H
#define DOCUMENTSCANNER_POSTPROCESS_H

#include <opencv2/core/mat.hpp>
namespace scanner {
    void contrast(cv::Mat& input, cv::Mat& output, float contrast);
    void brightness(cv::Mat& intput, cv::Mat& output, float brightness);
}
#endif //DOCUMENTSCANNER_POSTPROCESS_H
