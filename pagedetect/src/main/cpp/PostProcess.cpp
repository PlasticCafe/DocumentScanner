//
// Created by gamefreak on 2/21/19.
//

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include "PostProcess.h"

namespace scanner {
    void brightnessAndContrast(cv::Mat input, cv::Mat output, float brightness, float contrast) {
        if(brightness > 1.0) brightness = 1.0f;
        if(brightness < -1.0) brightness = -1.0f;
        contrast = contrast + 1;
        int8_t brightInt = brightness * 127;
        cv::Scalar brightFactor = cv::Scalar(brightInt, brightInt, brightInt, 0);
        cv::Scalar contrastFactor = cv::Scalar(contrast, contrast, contrast, 1);
        cv::multiply(input, contrastFactor, output);
        cv::add(output, brightFactor, output);
        return;
    }

    void threshold(cv::Mat input, cv::Mat output) {
        int frameScale = cvCeil(cv::sqrt(input.rows * input.cols)) | 1;
        cv::Mat gray;
        cv::cvtColor(input, gray, cv::COLOR_RGBA2GRAY);
        cv::GaussianBlur(gray, gray, cv::Size(frameScale / 200 | 1, frameScale / 200 | 1), 0);
        cv::adaptiveThreshold(gray, gray, 255, cv::ADAPTIVE_THRESH_MEAN_C, cv::THRESH_BINARY,
                              frameScale / 238 | 3, 2);
        //cv::Mat element = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(3, 3), cv::Point(-1, -1));
        //cv::morphologyEx(tmp, tmp, cv::MORPH_CLOSE, element);
        cv::cvtColor(gray, output, cv::COLOR_GRAY2RGBA);
    }
}
