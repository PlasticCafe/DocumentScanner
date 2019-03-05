//
// Created by gamefreak on 2/21/19.
//

#ifndef DOCUMENTSCANNER_POSTPROCESS_H
#define DOCUMENTSCANNER_POSTPROCESS_H

#include <opencv2/core/mat.hpp>
namespace scanner {
    void brightnessAndContrast(cv::Mat input, cv::Mat output, float brightness, float contrast);
    void threshold(cv::Mat input, cv::Mat output);
}
#endif //DOCUMENTSCANNER_POSTPROCESS_H
