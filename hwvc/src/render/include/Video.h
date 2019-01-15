/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_VIDEO_H
#define HARDWAREVIDEOCODEC_VIDEO_H

#include "Unit.h"
#include "TextureAllocator.h"
#include "AsynVideoDecoder.h"
#include "YUV420PFilter.h"
#include "EventPipeline.h"
#include "Egl.h"
#include "Frame.h"
#include "AudioPlayer.h"
#include "../entity/NativeWindow.h"

enum PlayState {
    PAUSE = 0,
    PLAYING = 1,
    STOP = -1
};

class Video : public Unit {
public:
    Video();

    virtual ~Video();

    virtual void release() override;

    bool eventPrepare(Message *msg);

    bool eventStart(Message *msg);

    bool eventPause(Message *msg);

    bool eventStop(Message *msg);

    bool eventInvalidate(Message *msg);

    bool eventSetSource(Message *msg);

private:
    EventPipeline *pipeline = nullptr;
    Egl *egl = nullptr;
    TextureAllocator *texAllocator = nullptr;
    AsynVideoDecoder *decoder = nullptr;
    Frame *frame = nullptr;
    YUV420PFilter *yuvFilter = nullptr;
    GLuint yuv[3];
    PlayState playState = STOP;
    Object lock;
    char *path;
    AudioPlayer *audioPlayer = nullptr;
    int64_t lastShowTime = 0;

    void loop();

    void checkFilter();

    int grab();

    void createAudioPlayer();

    void initEGL(NativeWindow *nw);
};


#endif //HARDWAREVIDEOCODEC_VIDEO_H
