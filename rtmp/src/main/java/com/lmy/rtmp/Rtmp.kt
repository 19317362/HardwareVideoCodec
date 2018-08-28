package com.lmy.rtmp

import android.os.Handler

interface Rtmp {
    //连接RTMP服务器
    fun connect(url: String, timeOut: Int, cacheSize: Int): Int

    //连接成功后默认打开stream
    fun connectStream(width: Int, height: Int): Int

    fun sendVideoSpecificData(sps: ByteArray, spsLen: Int, pps: ByteArray, ppsLen: Int): Int
    fun sendVideo(data: ByteArray, len: Int, timestamp: Long): Int
    fun sendAudioSpecificData(data: ByteArray, len: Int): Int
    fun sendAudio(data: ByteArray, len: Int, timestamp: Long): Int
    fun stop()
    fun setCacheSize(size: Int)
    fun setHandler(h: Handler)
    fun onJniError(error: Int)
}