package com.lmy.codec.decoder

import android.media.MediaExtractor
import android.media.MediaFormat
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Track
import com.lmy.codec.util.debug_e
import java.io.IOException

/**
 * Created by lmyooyo@gmail.com on 2018/10/15.
 */
class VideoExtractor(private val context: CodecContext,
                     private val inputPath: String) {
    private val videoExtractor = MediaExtractor()
    private val audioExtractor = MediaExtractor()
    private var videoTrack: Track? = null
    private var audioTrack: Track? = null

    init {
        prepareExtractor()
    }

    private fun prepareExtractor() {
        try {
            videoExtractor.setDataSource(this.inputPath)
            audioExtractor.setDataSource(this.inputPath)
        } catch (e: IOException) {
            debug_e("File(${context.ioContext.path}) not found")
            return
        }
        videoTrack = Track.getVideoTrack(videoExtractor)
        audioTrack = Track.getAudioTrack(audioExtractor)
        context.orientation = if (videoTrack!!.format.containsKey(VideoDecoder.KEY_ROTATION))
            videoTrack!!.format.getInteger(VideoDecoder.KEY_ROTATION) else 0
        if (context.isHorizontal()) {
            context.video.width = videoTrack!!.format.getInteger(MediaFormat.KEY_WIDTH)
            context.video.height = videoTrack!!.format.getInteger(MediaFormat.KEY_HEIGHT)
            context.cameraSize.width = context.video.width
            context.cameraSize.height = context.video.height
        } else {
            context.video.width = videoTrack!!.format.getInteger(MediaFormat.KEY_HEIGHT)
            context.video.height = videoTrack!!.format.getInteger(MediaFormat.KEY_WIDTH)
            context.cameraSize.width = context.video.height
            context.cameraSize.height = context.video.width
        }
    }

    fun seekTo(startUs: Long) {
        synchronized(videoExtractor) {
            videoExtractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        }
        synchronized(audioExtractor) {
            videoExtractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        }
    }

    fun range(startUs: Long, endUs: Long) {
        if (startUs >= endUs) {
            throw RuntimeException("endUs cannot smaller than startUs")
        }
        seekTo(startUs)
    }

    fun getVideoTrack(): Track? {
        return videoTrack
    }

    fun getAudioTrack(): Track? {
        return audioTrack
    }

    fun containAudio(): Boolean {
        return null != audioTrack
    }

    fun release() {
        videoExtractor.release()
        audioExtractor.release()
        videoTrack = null
        audioTrack = null
    }
}