/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_VIDEOPROCESSOR_H
#define HARDWAREVIDEOCODEC_VIDEOPROCESSOR_H

#include "Object.h"
#include "UnitPipeline.h"
#include "Screen.h"
#include "Filter.h"

class VideoProcessor : public Object {
public:
    VideoProcessor();

    virtual ~VideoProcessor();

    void prepare(ANativeWindow *win, int width, int height);

    void start();

private:
    UnitPipeline *pipeline = nullptr;
};


#endif //HARDWAREVIDEOCODEC_VIDEOPROCESSOR_H
