/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lmy.codec

import android.graphics.SurfaceTexture
import android.os.Environment
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CodecFactory
import com.lmy.codec.impl.AudioEncoderImpl
import com.lmy.codec.impl.MuxerImpl
import com.lmy.codec.render.Render
import com.lmy.codec.render.impl.DefaultRenderImpl
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.CameraWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraPreviewPresenter(var parameter: Parameter,
                             var encoder: Encoder? = null,
                             var audioEncoder: Encoder? = null,
                             private var cameraWrapper: CameraWrapper? = null,
                             private var render: Render? = null,
                             private var muxer: Muxer? = MuxerImpl("${Environment.getExternalStorageDirectory().absolutePath}/test.mp4"))
    : SurfaceTexture.OnFrameAvailableListener {

    private var onStateListener: OnStateListener? = null

    init {
        cameraWrapper = CameraWrapper.open(parameter, this)
        render = DefaultRenderImpl(parameter, cameraWrapper!!.textureWrapper as CameraTextureWrapper)
    }

    fun setFilter(filter: Class<*>) {
        render?.setFilter(filter)
    }

    fun getFilter(): BaseFilter? {
        return render?.getFilter()
    }

    /**
     * Camera有数据生成时回调
     * For CameraWrapper
     */
    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        render?.onFrameAvailable()?.afterRender(Runnable {
            encoder?.onFrameAvailable(cameraTexture)
        })
    }

    fun startPreview(screenTexture: SurfaceTexture, width: Int, height: Int) {
        cameraWrapper!!.startPreview()
        render?.start(screenTexture, width, height, Runnable {
            encoder = CodecFactory.getEncoder(parameter, render!!.getFrameBufferTexture(),
                    cameraWrapper!!.textureWrapper.egl!!.eglContext!!)
            audioEncoder = AudioEncoderImpl(parameter)
            if (null != muxer) {
                encoder!!.setOnSampleListener(muxer!!)
                audioEncoder!!.setOnSampleListener(muxer!!)
            }
        })
    }

    fun updatePreview(width: Int, height: Int) {
//        mRender?.updatePreview(width, height)
    }

    fun stopPreview() {
        release()
    }

    private fun release() {
        stopEncoder()
        try {
            render?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            cameraWrapper?.release()
            cameraWrapper = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopEncoder() {
        encoder?.stop(object : Encoder.OnStopListener {
            override fun onStop() {
                audioEncoder?.stop(object : Encoder.OnStopListener {
                    override fun onStop() {
                        muxer?.release()
                        onStateListener?.onStop()
                    }
                })
            }
        })
    }

    fun setOnStateListener(listener: OnStateListener) {
        onStateListener = listener
    }

    interface OnStateListener {
        fun onStop()
    }
}