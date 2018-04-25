/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.entity.Parameter
import com.lmy.codec.helper.CameraHelper
import com.lmy.codec.util.debug_e
import com.lmy.codec.util.debug_v

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
class CameraWrapper(private var parameter: Parameter,
                    private var onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener,
                    var textureWrapper: TextureWrapper = CameraTextureWrapper()) {
    companion object {
        private val PREPARE = 0x1
        fun open(param: Parameter, onFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener)
                : CameraWrapper {
            return CameraWrapper(param, onFrameAvailableListener)
        }
    }

    private var mHandlerThread = HandlerThread("Renderer_Thread")
    private var mHandler: Handler? = null
    private var mCamera: Camera? = null
    private var mCameras = 0
    private var mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK

    private var mPrepare = false
    private var mRequestPreview = false

    init {
        mCameras = CameraHelper.getNumberOfCameras()
        initThread()
        mHandler?.removeMessages(PREPARE)
        mHandler?.sendEmptyMessage(PREPARE)
    }

    private fun initThread() {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    PREPARE -> {
                        prepare()
                    }
                }
            }
        }
    }

    private fun prepare() {
        if (0 == mCameras) {
            debug_e("Unavailable camera")
            return
        }
        //如果没有前置摄像头，则强制使用后置摄像头
        if (parameter.cameraIndex == Camera.CameraInfo.CAMERA_FACING_FRONT && mCameras < 2)
            parameter.cameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK
        mCameraIndex = parameter.cameraIndex

        val time = System.currentTimeMillis()
        mCamera = openCamera(mCameraIndex)
        debug_e("open time: ${System.currentTimeMillis() - time}")
        if (null == mCamera) return
        val cameraParam = mCamera!!.parameters
        CameraHelper.setPreviewSize(cameraParam, parameter)
        CameraHelper.setColorFormat(cameraParam, parameter)
        CameraHelper.setFocusMode(cameraParam, parameter)
        CameraHelper.setFps(cameraParam, parameter)
        CameraHelper.setAutoExposureLock(cameraParam, false)
        CameraHelper.setSceneMode(cameraParam, Camera.Parameters.SCENE_MODE_AUTO)
        CameraHelper.setFlashMode(cameraParam, Camera.Parameters.FLASH_MODE_OFF)
        CameraHelper.setAntibanding(cameraParam, Camera.Parameters.ANTIBANDING_AUTO)
        CameraHelper.setVideoStabilization(cameraParam, true)
        val fps = IntArray(2)
        cameraParam.getPreviewFpsRange(fps)
        debug_v("Config: Size(${parameter.previewWidth}x${parameter.previewHeight})\n" +
                "Format(${cameraParam.previewFormat})\n" +
                "FocusMode(${cameraParam.focusMode})\n" +
                "Fps(${fps[0]}-${fps[1]})\n" +
                "AutoExposureLock(${cameraParam.autoExposureLock})\n" +
                "SceneMode(${cameraParam.sceneMode})\n" +
                "FlashMode(${cameraParam.flashMode})\n" +
                "Antibanding(${cameraParam.antibanding})\n" +
                "VideoStabilization(${cameraParam.videoStabilization})")
        try {
            mCamera!!.parameters = cameraParam
            mPrepare = true
            if (mRequestPreview) {
                startPreview()
                mRequestPreview = false
                mPrepare = false
            }
        } catch (e: Exception) {
            mCamera!!.release()
            debug_e("Camera $mCameraIndex open failed. Please check parameters")
        }
    }

    private fun openCamera(index: Int): Camera? {
        return try {
            val camera = Camera.open(index)
            parameter.check()
            camera.setDisplayOrientation(parameter.orientation)
            camera
        } catch (e: SecurityException) {
            debug_e("Camera $index open failed, No permission")
            e.printStackTrace()
            null
        } catch (e: Exception) {
            debug_e("Camera $index open failed")
            e.printStackTrace()
            null
        }
    }

    fun release() {
        if (null == mCamera) return
        stopPreview()
        releaseTexture()
        mCamera!!.release()
        mCamera = null
    }

    fun startPreview(): Boolean {
        mRequestPreview = true
        if (!mPrepare) return false
        if (null == mCamera) return false
        textureWrapper.surfaceTexture!!.setOnFrameAvailableListener(onFrameAvailableListener)
        return try {
            mCamera!!.setPreviewTexture(textureWrapper.surfaceTexture)
            mCamera!!.startPreview()
            true
        } catch (e: Exception) {
            release()
            debug_e("Start preview failed")
            e.printStackTrace()
            false
        }
    }

    private fun stopPreview() {
        if (null == mCamera) return
        try {
            mCamera!!.stopPreview()
        } catch (e: Exception) {
            debug_e("Stop preview failed")
            e.printStackTrace()
        }
        mHandlerThread.quitSafely()
    }

    private fun releaseTexture() {
        textureWrapper.release()
    }
}