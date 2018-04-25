package com.lmy.codec.entity

import android.content.Context
import android.hardware.Camera
import android.media.AudioFormat
import android.media.MediaCodecInfo

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
data class Parameter(var context: Context,
                     var video: Video = Video(),
                     var audio: Audio = Audio(),
                     var cameraIndex: Int = Camera.CameraInfo.CAMERA_FACING_BACK,
                     var previewWidth: Int = 1280,//以水平分辨率为准
                     var previewHeight: Int = 720,//以水平分辨率为准
                     var orientation: Int = 90,
                     var codecType: CodecType = CodecType.HARD) {
    fun check() {
        if (!isHorizontal() && !isVertical())
            throw RuntimeException("Orientation must be 0, 90, 180 or 270")
        if (isVertical() && (video.width > previewHeight || video.height > previewWidth)
                || isHorizontal() && (video.width > previewWidth || video.height > previewHeight))
            throw RuntimeException("Video size can not be greater than preview size")
    }

    fun isHorizontal(): Boolean {
        return 0 == orientation || 180 == orientation
    }

    fun isVertical(): Boolean {
        return 90 == orientation || 270 == orientation
    }

    data class Video(var mime: String = "video/avc",
                     var width: Int = 720,
                     var height: Int = 480,
                     var fps: Int = 24,
                     var bitrate: Int = width * height * 3,
                     var iFrameInterval: Int = 2,
                     var bitrateMode: Int = 1,//if support, default VBR
                     var profile: Int = 0x08,//if support, default High
                     var level: Int = 0x200//if support, default level 31
    )

    data class Audio(var mime: String = "audio/mp4a-latm",
                     var channel: Int = 1,
                     var samplePerFrame: Int = 1024,
                     var sampleRateInHz: Int = 16000,
                     var bitrate: Int = 64000,
                     var profile: Int = MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                     var pcm: Int = AudioFormat.ENCODING_PCM_16BIT)

    enum class CodecType {
        HARD, SOFT
    }
}