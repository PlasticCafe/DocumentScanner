
//
// Created by gamefreak on 12/13/2018.
//

#ifndef DOCUMENTSCANNER_PAGEDETECTOR_H
#define DOCUMENTSCANNER_PAGEDETECTOR_H

#include <vector>
#include <opencv2/core/types.hpp>
#include <opencv2/core/mat.hpp>
#ifdef __ANDROID_NDK__
#include <jni.h>
#endif
namespace scanner {
    class PageDetector {
    public:
        PageDetector(float scale = 0.25);

        std::vector<cv::Point>
        detect_nv21(std::vector<uint8_t> &frameInput, int32_t width, int32_t height);

        std::vector<cv::Point> detect(cv::Mat &frameInput);

        cv::Mat getMat_nv21(std::vector<uint8_t> &frameInput, int32_t width, int32_t height);

        cv::Mat getFrameSection(cv::Mat &frameInput, std::vector<cv::Point>);

        float getArea(std::vector<cv::Point> &points);

        double distortion(std::vector<cv::Point> &points);


    private:
        float scale;


        cv::Mat edgeDetect(cv::Mat &input);

        void makePointsClockwise(std::vector<cv::Point> &points);

        //bool detectPerspectiveDistortion(std::vector<cv::Point> &points);

        void alignTopEdge(std::vector<cv::Point> &points);

        std::vector<cv::Point> findPage(cv::Mat &edges);

        int getMatScale(cv::Mat &frameInput);
    };
}

#endif //DOCUMENTSCANNER_PAGEDETECTOR_H