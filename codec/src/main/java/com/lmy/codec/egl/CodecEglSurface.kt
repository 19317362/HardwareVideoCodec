/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.egl

import android.opengl.EGLContext
import android.view.Surface
import com.lmy.codec.egl.entity.Egl
import com.lmy.codec.entity.CodecContext
import com.lmy.codec.texture.impl.NormalTexture
import com.lmy.codec.util.debug_e

/**
 * Created by lmyooyo@gmail.com on 2018/3/28.
 */
class CodecEglSurface private constructor(eglContext: EGLContext?,
                                          surface: Surface,
                                          textureId: IntArray?) : EglInputSurface() {

    init {
        this.surface = surface
        this.textureId = textureId
        egl = Egl("Codec")
        egl!!.initEGL(surface, eglContext)
        egl!!.makeCurrent()
        if (null == textureId)
            throw RuntimeException("textureId can not be null")
        texture = NormalTexture(textureId!!).apply {
            name = "Codec Texture"
        }
    }

    override fun draw(transformMatrix: FloatArray?) {
        if (null == texture) {
            debug_e("Render failed. Texture is null")
            return
        }
        texture?.draw(transformMatrix)
    }

    override fun release() {
        super.release()
        surface?.release()
    }

    override fun updateLocation(context: CodecContext) {

    }

    companion object {
        fun create(surface: Surface, textureId: IntArray?,
                   eglContext: EGLContext? = null): EglInputSurface = CodecEglSurface(eglContext, surface, textureId)
    }
}