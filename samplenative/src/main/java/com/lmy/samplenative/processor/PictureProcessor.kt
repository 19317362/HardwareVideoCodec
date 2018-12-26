package com.lmy.samplenative.processor

import android.view.Surface

class PictureProcessor {
    private var handler: Long = 0

    init {
        handler = create()
    }

    fun prepare(surface: Surface, width: Int, height: Int) {
        if (0L == handler) return
        prepare(handler, surface, width, height)
    }

    fun show(rgba: ByteArray, width: Int, height: Int) {
        if (0L == handler) return
        show(handler, rgba, width, height)
    }

    fun release() {
        release(handler)
        handler = 0
    }

    private external fun create(): Long
    private external fun prepare(handler: Long, surface: Surface, width: Int, height: Int)
    private external fun show(handler: Long, rgba: ByteArray, width: Int, height: Int)
    private external fun release(handler: Long)
}