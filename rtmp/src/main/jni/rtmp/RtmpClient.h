//
// Created by nonolive66 on 2018/7/25.
//
#include <log.h>
#include <string.h>
#include "librtmp/rtmp.h"
#include "SpecificData.h"
#include "EventPipeline.h"

#ifndef HARDWAREVIDEOCODEC_RTMP_H
#define HARDWAREVIDEOCODEC_RTMP_H

#define ERROR_DISCONNECT  -100

class RtmpClient {
public:
    RtmpClient();

    /**
     * 连接rtmp服务
     */
    int connect(char *url, int timeOut);

    int _connect(char *url, int timeOut);

    /**
     * 新建流连接
     */
    int connectStream(int w, int h);

    int _connectStream(int w, int h);

    /**
     * 删除流连接
     */
    void deleteStream();

    /**
     * 发送sps、pps 帧
     */
    int sendVideoSpecificData(char *sps, int spsLen, char *pps, int ppsLen);

    int _sendVideoSpecificData();

    /**
     * 发送视频帧
     */
    int sendVideo(char *data, int len, long timestamp);

    int _sendVideo(char *data, int len, long timestamp);

    /**
     * 发送音频关键帧
     */
    int sendAudioSpecificData(char *data, int len);

    int _sendAudioSpecificData();

    /**
     * 发送音频数据
     */
    int sendAudio(char *data, int len, long timestamp);

    int _sendAudio(char *data, int len, long timestamp);

    /**
     * 释放资源
     */
    void stop();

    ~RtmpClient();

private:
    EventPipeline *pipeline = NULL;
    SpecificData *sps = NULL, *pps = NULL, *spec = NULL;
    long videoCount = 0, audioCount = 0;
    long retryTime[3] = {3000, 9000, 27000};
    int width;
    int height;
    int timeOut;
    char *url;
    long startTime;
    RTMP *rtmp;

    void saveVideoSpecificData(char *sps, int spsLen, char *pps, int ppsLen);

    void saveAudioSpecificData(char *spec, int len);

    int sendVideoSpecificData(SpecificData *sps, SpecificData *pps);

    int sendAudioSpecificData(SpecificData *spec);
};

class Connection {
public:
    RtmpClient *client;
    char *url;
    int timeOut;
};

class Size {
public:
    RtmpClient *client;
    int width, height;
};

class Packet {
public:
    RtmpClient *client;
    char *data;
    int size;
    long timestamp;
    ~Packet(){
        if(NULL!=data){
            free(data);
            data=NULL;
        }
    }
};

#endif //HARDWAREVIDEOCODEC_RTMP_H
