//
// Created by gamefreak on 2/21/19.
//

#include <opencv2/core.hpp>
#include "PostProcess.h"

namespace scanner {
    void contrast(cv::Mat &input, cv::Mat &output, float contrast) {

    }

    void brightness(cv::Mat &input, cv::Mat &output, float brightness) {
        if (brightness > 1.0 || brightness < -1.0) {
            input.copyTo(output);
            return;
        }
        int8_t brightInt = brightness * 255;
        cv::Scalar brightFactor = cv::Scalar(brightInt, brightInt, brightInt);
        cv::add(input, brightFactor, output);
        return;
    }
}
