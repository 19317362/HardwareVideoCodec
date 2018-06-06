package com.lmy.codec.texture.impl.filter

/**
 * Created by lmyooyo@gmail.com on 2018/6/6.
 */
class HalftoneFilter(width: Int = 0,
                     height: Int = 0,
                     textureId: Int = -1,
                     private var mFractionalWidthOfAPixel: Float = 0f,
                     private var mAspectRatio: Float = 0f) : BaseFilter(width, height, textureId) {

    private var aPositionLocation = 0
    private var uTextureLocation = 0
    private var aTextureCoordinateLocation = 0
    private var mFractionalWidthOfPixelLocation: Int = 0
    private var mAspectRatioLocation: Int = 0


    override fun init() {
        super.init()
        aPositionLocation = getAttribLocation("aPosition")
        uTextureLocation = getUniformLocation("uTexture")
        aTextureCoordinateLocation = getAttribLocation("aTextureCoord")
        mFractionalWidthOfPixelLocation = getUniformLocation("fractionalWidthOfPixel")
        mAspectRatioLocation = getUniformLocation("aspectRatio")
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        active()
        uniform1i(uTextureLocation, 0)
        setUniform1f(mFractionalWidthOfPixelLocation, mFractionalWidthOfAPixel)
        setUniform1f(mAspectRatioLocation, mAspectRatio)
        enableVertex(aPositionLocation, aTextureCoordinateLocation)
        draw()
        disableVertex(aPositionLocation, aTextureCoordinateLocation)
        inactive()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.sh"
    }

    override fun getFragment(): String {
        return "shader/fragment_halftone.sh"
    }

    override fun setValue(index: Int, value: Int) {
        when (index) {
            0 -> {
                mFractionalWidthOfAPixel = value / 100f * 0.1f
            }
            1 -> {
                mAspectRatio = value / 100f * 10
            }
        }
    }
}