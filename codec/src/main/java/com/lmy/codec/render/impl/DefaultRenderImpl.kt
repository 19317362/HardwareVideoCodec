package com.lmy.codec.render.impl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.lmy.codec.entity.Parameter
import com.lmy.codec.render.Render
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.NormalTexture
import com.lmy.codec.texture.impl.filter.GreyTextureFilter
import com.lmy.codec.util.debug_e
import com.lmy.codec.wrapper.CameraTextureWrapper
import com.lmy.codec.wrapper.ScreenTextureWrapper

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
class DefaultRenderImpl(var parameter: Parameter,
                        var cameraWrapper: CameraTextureWrapper,
                        var transformMatrix: FloatArray = FloatArray(16),
                        var screenTexture: SurfaceTexture? = null,
                        var screenWrapper: ScreenTextureWrapper? = null,
                        private var runnable: Runnable? = null,
                        var width: Int = 1,
                        var height: Int = 1,
                        private var viewportX: Int = 0,
                        private var viewportY: Int = 0,
                        var cameraWidth: Int = 0,
                        var cameraHeight: Int = 0)
    : Render {

    companion object {
        val INIT = 0x1
        val RENDER = 0x2
        val STOP = 0x3
    }

    private lateinit var filter: BaseFrameBufferTexture
    private var mHandlerThread = HandlerThread("Renderer_Thread")
    private var mHandler: Handler? = null

    init {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    INIT -> {
                        init()
                        if (null != msg.obj) {
                            (msg.obj as Runnable).run()
                        }
                    }
                    RENDER -> {
                        draw()
                    }
                    STOP -> {
                        mHandlerThread.quitSafely()
                        screenWrapper?.release()
                    }
                }
            }
        }
    }

    fun init() {
        cameraWrapper.initEGL(parameter.video.width, parameter.video.height)
        filter = GreyTextureFilter(parameter.video.width, parameter.video.height)
        filter.textureId = cameraWrapper.getFrameBufferTexture()
        screenWrapper = ScreenTextureWrapper(screenTexture, cameraWrapper.egl!!.eglContext!!)
        screenWrapper?.setFilter(NormalTexture(filter.frameBufferTexture!!))
//        (screenWrapper!!.texture as BeautyTexture).setParams(0f, -5f)//beauty: 0 - 2.5, tone: -5 - 5
//        (screenWrapper!!.texture as BeautyTexture).setBrightLevel(0f)//0 - 1
//        (screenWrapper!!.texture as BeautyTexture).setTexelOffset(-10f)//-10 - 10
    }

    override fun draw() {
        drawCamera()
        drawFilter()
        screenWrapper?.egl?.makeCurrent()
        GLES20.glViewport(viewportX, viewportY, width, height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        screenWrapper?.drawTexture(transformMatrix)
        screenWrapper?.egl?.swapBuffers()
        runnable?.run()
    }

    private fun drawFilter() {
        GLES20.glViewport(0, 0, parameter.video.width, parameter.video.height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        filter.drawTexture(null)
    }

    private fun drawCamera() {
        if (null != cameraWrapper.surfaceTexture) {
            cameraWrapper.surfaceTexture?.updateTexImage()
            cameraWrapper.surfaceTexture?.getTransformMatrix(transformMatrix)
        }
        cameraWrapper.egl?.makeCurrent("cameraWrapper")
        GLES20.glViewport(0, 0, parameter.previewHeight, parameter.previewWidth)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 0f)
        cameraWrapper.drawTexture(transformMatrix)
    }

    override fun start(texture: SurfaceTexture, width: Int, height: Int) {
        start(texture, width, height, null)
    }

    override fun start(texture: SurfaceTexture, width: Int, height: Int, runnable: Runnable?) {
        updateScreenTexture(texture)
        initViewport(width, height)
        if (mHandlerThread.isAlive)
            mHandler?.sendMessage(mHandler!!.obtainMessage(INIT, runnable))
    }

    private fun initViewport(width: Int, height: Int) {
//        this.width = width
//        this.height = height
        initCameraViewport(width, height)
        debug_e("initViewport($width, $height): before")
        val videoRatio = parameter.video.width / parameter.video.height.toFloat()
        val viewRatio = width / height.toFloat()
        if (videoRatio > viewRatio) {//以View的宽为准
            this.width = width
            this.height = (width / videoRatio).toInt()
        } else {//以View的高为准
            this.width = (height * videoRatio).toInt()
            this.height = height
        }
        viewportX = (width - this.width) / 2
        viewportY = (height - this.height) / 2
        debug_e("initViewport(${this.viewportX}, ${this.viewportY})(${this.width}, ${this.height}): after")
    }

    private fun initCameraViewport(width: Int, height: Int) {
        cameraWidth = width
        cameraHeight = height
        //摄像头宽高以横屏为准
        val cameraRatio = parameter.previewHeight / parameter.previewWidth.toFloat()
        val viewRatio = width / height.toFloat()
        if (cameraRatio < viewRatio) {//高度被压缩了，以View的宽为准
            this.cameraWidth = width
            this.cameraHeight = (width / cameraRatio).toInt()
        } else {
            this.cameraWidth = (height * cameraRatio).toInt()
            this.cameraHeight = height
        }
        debug_e("initCameraViewport(${this.cameraWidth}, ${this.cameraHeight})")
    }

    override fun stop() {
        try {
            if (mHandlerThread.isAlive)
                mHandler?.sendEmptyMessage(STOP)
        } catch (e: Exception) {
        }
    }

    override fun release() {
        stop()
    }

    override fun onFrameAvailable(): Render {
        try {
            if (mHandlerThread.isAlive)
                mHandler?.sendEmptyMessage(RENDER)
        } catch (e: Exception) {
        }
        return this
    }

    fun updateScreenTexture(texture: SurfaceTexture?) {
        screenTexture = texture
    }

    override fun afterRender(runnable: Runnable) {
        this.runnable = runnable
    }

    override fun setFilter(filter: BaseFrameBufferTexture) {
        this.filter = filter
    }

    override fun getFrameBuffer(): Int {
        if (null != filter) return filter!!.frameBuffer!!
        return cameraWrapper.getFrameBuffer()
    }

    override fun getFrameBufferTexture(): Int {
        if (null != filter) return filter!!.frameBufferTexture!!
        return cameraWrapper.getFrameBufferTexture()
    }
}