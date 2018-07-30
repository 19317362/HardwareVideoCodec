//
// Created by limin on 2018/7/26.
//
#include <string.h>

#ifndef HARDWAREVIDEOCODEC_SPECIFICDATA_H
#define HARDWAREVIDEOCODEC_SPECIFICDATA_H


class SpecificData {
public:
    SpecificData(char *data, int size);

    char *get();

    int size();

    bool alreadySent();

    void setSent(bool sent);

    ~SpecificData();

private:
    char *data = NULL;
    int s;
    bool sent = false;
};


#endif //HARDWAREVIDEOCODEC_SPECIFICDATA_H
