/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl

import android.opengl.GLES11Ext
import android.opengl.GLES20

/**
 * Created by lmyooyo@gmail.com on 2018/4/20.
 */
class MirrorTexture(width: Int, height: Int,
                    textureId: Int,
                    private var direction: Direction = Direction.VERTICAL)
    : BaseFrameBufferTexture(width, height, textureId) {
    enum class Direction {
        VERTICAL, HORIZONTAL
    }

    companion object {
        private val VERTEX_SHADER = "" +
                "attribute vec4 aPosition;\n" +
                "attribute vec2 aTextureCoord;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main(){\n" +
                "    gl_Position= aPosition;\n" +
                "    vTextureCoord = aTextureCoord;\n" +
                "}"
        private val FRAGMENT_SHADER = "" +
                "precision mediump float;\n" +
                "varying mediump vec2 vTextureCoord;\n" +
                "uniform sampler2D uTexture;\n" +
                "void main(){\n" +
                "    vec4  color = texture2D(uTexture, vTextureCoord);\n" +
                "    gl_FragColor = color;\n" +
                "}"
        private val VERTICES_VERTICAL = floatArrayOf(
                0f, 0f,
                0f, 1f,
                1f, 1f,
                1f, 0f)
        private val VERTICES_HORIZONTAL = floatArrayOf(
                1f, 1f,
                1f, 0f,
                0f, 0f,
                0f, 1f)
    }

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0

    init {
        verticesBuffer = createShapeVerticesBuffer(
                if (Direction.VERTICAL == direction) VERTICES_VERTICAL
                else VERTICES_HORIZONTAL)

        createProgram()
        initFrameBuffer()
    }

    private fun createProgram() {
        shaderProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!!)
        GLES20.glUseProgram(shaderProgram!!)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTextureLocation, 0)
        enableVertex(aPositionLocation, aTextureCoordinateLocation, buffer!!, verticesBuffer!!)

        drawer.draw()

        GLES20.glFinish()
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisableVertexAttribArray(aTextureCoordinateLocation)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NONE)
        GLES20.glUseProgram(GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
    }
}