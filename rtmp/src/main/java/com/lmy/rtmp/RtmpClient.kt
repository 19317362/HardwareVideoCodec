package com.lmy.rtmp

import android.util.Log

/**
 * Created by lmyooyo@gmail.com on 2018/7/25.
 */
class RtmpClient : Rtmp {

    init {
        System.loadLibrary("rtmp")
    }

    override fun onJniError(error: Int) {
        Log.e("RtmpClient", "onJniError $error")
    }

    external override fun connect(url: String, timeOut: Int, cacheSize: Int): Int
    external override fun connectStream(width: Int, height: Int): Int
    external override fun sendVideoSpecificData(sps: ByteArray, spsLen: Int, pps: ByteArray, ppsLen: Int): Int
    external override fun sendVideo(data: ByteArray, len: Int, timestamp: Long): Int
    external override fun sendAudioSpecificData(data: ByteArray, len: Int): Int
    external override fun sendAudio(data: ByteArray, len: Int, timestamp: Long): Int
    external override fun stop()
    external override fun setCacheSize(size: Int)

    companion object {
        fun build(): RtmpClient {
            return RtmpClient()
        }
    }
}