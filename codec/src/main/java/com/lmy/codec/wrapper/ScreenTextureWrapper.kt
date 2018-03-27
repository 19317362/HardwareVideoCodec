package com.lmy.codec.wrapper

import android.graphics.SurfaceTexture
import com.lmy.codec.entity.Egl
import com.lmy.codec.texture.impl.BaseTexture
import com.lmy.codec.util.debug_e


/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
class ScreenTextureWrapper(override var surfaceTexture: SurfaceTexture? = null,
                           private var texture: BaseTexture? = null,
                           var egl: Egl? = null) : TextureWrapper() {
    init {
        if (null != surfaceTexture) {
            egl = Egl()
            egl!!.initEGL(surfaceTexture!!)
            egl!!.makeCurrent()
        } else {
            debug_e("Egl create failed")
        }
    }

    fun setFilter(texture: BaseTexture) {
        this.texture = texture
    }

    fun drawTexture(transformMatrix: FloatArray) {
        if (null == texture) {
            debug_e("Render failed. Texture is null")
            return
        }
        texture?.drawTexture(transformMatrix)
    }
}