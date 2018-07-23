/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.render.impl

import android.graphics.Point
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Environment
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.entity.Size
import com.lmy.codec.helper.PixelsReader
import com.lmy.codec.pipeline.SingleEventPipeline
import com.lmy.codec.render.Render
import com.lmy.codec.texture.impl.filter.BaseFilter
import com.lmy.codec.texture.impl.filter.NormalFilter
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.ScreenTextureWrapper


/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class DefaultRenderImpl(var context: CodecContext,
                        var cameraWrapper: CameraTextureWrapper,
                        var transformMatrix: FloatArray = FloatArray(16),
                        var screenTexture: SurfaceTexture? = null,
                        var screenWrapper: ScreenTextureWrapper? = null,
                        var reader: PixelsReader? = null,
                        private var viewport: Viewport = Viewport())
    : Render {

    private val filterLock = Any()
    private var filter: BaseFilter? = null

    fun init() {
        initReader()
        initFilter(NormalFilter::class.java)
    }

    private fun initFilter(clazz: Class<*>) {
        synchronized(filterLock) {
            try {
                filter = clazz.newInstance() as BaseFilter
            } catch (e: Exception) {
                e.printStackTrace()
                initScreen()
                return
            }
            filter?.width = context.video.width
            filter?.height = context.video.height
//            debug_e("camera texture: ${cameraWrapper.getFrameBuffer()},${cameraWrapper.getFrameBufferTexture()}")
            filter?.textureId = cameraWrapper.getFrameBufferTexture()
            filter?.init()
        }
        initScreen()
    }

    private fun initReader() {
        reader?.stop()
        reader = PixelsReader.create(context.context, context.video.width, context.video.height)
        reader?.start()
    }

    private fun initScreen() {
        if (null == screenWrapper) {
            screenWrapper = ScreenTextureWrapper(screenTexture, getFrameBufferTexture(),
                    cameraWrapper.egl!!.eglContext!!)
        }
    }

    override fun draw() {
        if (null == screenWrapper) return
        drawCamera()
        drawFilter()
        screenWrapper?.egl?.makeCurrent()
        GLES20.glViewport(viewport.point.x, viewport.point.y, viewport.size.width, viewport.size.height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        screenWrapper?.drawTexture(transformMatrix)
        screenWrapper?.egl?.swapBuffers()
//        save()
    }

    private fun drawFilter() {
        synchronized(filterLock) {
            GLES20.glViewport(0, 0, context.video.width, context.video.height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
            filter?.drawTexture(null)
        }
    }

    private fun drawCamera() {
        if (null != cameraWrapper.surfaceTexture) {
            cameraWrapper.surfaceTexture?.updateTexImage()
            cameraWrapper.surfaceTexture?.getTransformMatrix(transformMatrix)
        }
        cameraWrapper.egl?.makeCurrent()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        cameraWrapper.drawTexture(transformMatrix)
        ++count
        if (0 == count % 60) {
            reader?.readPixels(cameraWrapper.getFrameBuffer())
            reader?.shoot("${Environment.getExternalStorageDirectory().path}/temp.jpg")
            reader?.recycleBuffer()
        }
    }

    private var count = 0

    override fun start(texture: SurfaceTexture, width: Int, height: Int) {
        updateScreenTexture(texture)
        viewport.setViewSize(width, height)
        viewport.reset(context)
        SingleEventPipeline.instance.queueEvent(Runnable { init() })
    }

    override fun updateSize(width: Int, height: Int) {
        context.video.width = width
        context.video.height = height
        viewport.reset(context)
        SingleEventPipeline.instance.queueEvent(Runnable {
            initReader()
            cameraWrapper.updateSize(context.previewWidth, context.previewHeight,
                    context.video.width, context.video.height)
            initFilter(NormalFilter::class.java)
        })
    }

    override fun stop() {
        SingleEventPipeline.instance.queueEvent(Runnable {
            screenWrapper?.release()
            screenWrapper = null
            filter?.release()
        })
    }

    override fun release() {
        reader?.stop()
        stop()
    }

    override fun onFrameAvailable() {
        SingleEventPipeline.instance.queueEvent(Runnable { draw() })
    }

    fun updateScreenTexture(texture: SurfaceTexture?) {
        screenTexture = texture
    }

    override fun post(runnable: Runnable) {
        SingleEventPipeline.instance.queueEvent(runnable)
    }

    override fun setFilter(filter: Class<*>) {
        SingleEventPipeline.instance.queueEvent(Runnable {
            initFilter(filter)
        })
    }

    override fun getFilter(): BaseFilter? {
        synchronized(filterLock) {
            return filter
        }
    }

    override fun getFrameBuffer(): Int {
        synchronized(filterLock) {
            if (null != filter) return filter!!.frameBuffer!!
        }
        return cameraWrapper.getFrameBuffer()

    }

    override fun getFrameBufferTexture(): Int {
        synchronized(filterLock) {
            if (null != filter) return filter!!.frameBufferTexture!!
        }
        return cameraWrapper.getFrameBufferTexture()
    }

    class Viewport(
            var point: Point = Point(0, 0),
            var size: Size = Size(0, 0),
            var viewSize: Size = Size(0, 0),
            var cameraSize: Size = Size(0, 0)) {
        private fun check() {
            if (viewSize.width < 1 || viewSize.height < 1)
                throw RuntimeException("You must set view size before reset!")
        }

        fun setViewSize(width: Int, height: Int) {
            viewSize.width = width
            viewSize.height = height
        }

        fun reset(codecContext: CodecContext) {
            check()
            reset(codecContext, viewSize.width, viewSize.height)
        }

        private fun reset(codecContext: CodecContext, width: Int, height: Int) {
            initCameraViewport(codecContext, width, height)
//            debug_e("initViewport($width, $height): before")
            val videoRatio = codecContext.video.width / codecContext.video.height.toFloat()
            val viewRatio = width / height.toFloat()
            if (videoRatio > viewRatio) {//以View的宽为准
                size.width = width
                size.height = (width / videoRatio).toInt()
            } else {//以View的高为准
                size.width = (height * videoRatio).toInt()
                size.height = height
            }
            point.x = (width - size.width) / 2
            point.y = (height - size.height) / 2
            debug_e("initViewport(${point.x}, ${point.y})" +
                    "(${size.width}, ${size.height})" +
                    "(${codecContext.video.width}, ${codecContext.video.height}): after")
        }

        private fun initCameraViewport(codecContext: CodecContext, width: Int, height: Int) {
            cameraSize.width = width
            cameraSize.height = height
            //摄像头宽高以横屏为准
            val cameraRatio = codecContext.previewHeight / codecContext.previewWidth.toFloat()
            val viewRatio = width / height.toFloat()
            if (cameraRatio < viewRatio) {//高度被压缩了，以View的宽为准
                cameraSize.width = width
                cameraSize.height = (width / cameraRatio).toInt()
            } else {
                cameraSize.width = (height * cameraRatio).toInt()
                cameraSize.height = height
            }
//            debug_e("initCameraViewport(${cameraSize.width}, ${cameraSize.height})")
        }
    }
}