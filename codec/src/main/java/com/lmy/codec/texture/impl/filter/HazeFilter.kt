package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/6/6.
 */
class HazeFilter(width: Int = 0,
                 height: Int = 0,
                 textureId: Int = -1,
                 private var mDistance: Float = 0f,
                 private var mSlope: Float = 0f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mDistanceLocation: Int = 0
    private var mSlopeLocation: Int = 0

    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mDistanceLocation = getUniformLocation("distance")
        mSlopeLocation = getUniformLocation("slope")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        uniform1i(uTextureLocation, 0)
        setUniform1f(mDistanceLocation, mDistance)
        setUniform1f(mSlopeLocation, mSlope)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_haze.sh"
    }

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mDistance = (value - 50) / 100f * 0.6f
            }
            1 -> {
                mSlope = (value - 50) / 100f * 0.6f
            }
        }
    }
}