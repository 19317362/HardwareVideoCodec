package com.lmy.codec.impl

import android.graphics.SurfaceTexture
import com.lmy.codec.IRecorder
import com.lmy.codec.Render
import com.lmy.codec.entity.Parameter
import com.lmy.codec.wrapper.CameraWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class VideoRecorder : IRecorder, SurfaceTexture.OnFrameAvailableListener {

    private val syncOp = Any()
    private var mCameraWrapper: CameraWrapper? = null
    private var mRender: Render? = null
    private var isPreviewing: Boolean = false
    override fun prepare(param: Parameter) {
        mCameraWrapper = CameraWrapper.open(param, this)
        mRender = Render()
    }

    /**
     * Camera有数据生成时回调
     * For CameraWrapper
     */
    override fun onFrameAvailable(cameraTexture: SurfaceTexture?) {
        mRender?.onFrameAvailable(cameraTexture)
    }

    override fun startPreview(screenTexture: SurfaceTexture, width: Int, height: Int) {
        synchronized(syncOp) {
            if (!isPreviewing) {
                if (!mCameraWrapper!!.startPreview()) {
                    return
                }
            }
            mRender?.start(screenTexture, width, height)
            isPreviewing = true
        }
    }

    override fun updatePreview(width: Int, height: Int) {
//        mRender?.updatePreview(width, height)
    }

    override fun stopPreview() {
        synchronized(syncOp) {
            if (isPreviewing) {
                mRender?.stop()
                mCameraWrapper?.stopPreview()
                mCameraWrapper?.releaseTexture()
            }
            isPreviewing = false
        }
    }

    interface OnVideoDataCallback {
        fun onFrame()
        fun onAudio()
    }

    override fun release() {
        synchronized(syncOp) {
            try {
                mCameraWrapper?.release()
                mCameraWrapper = null
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                mRender?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}