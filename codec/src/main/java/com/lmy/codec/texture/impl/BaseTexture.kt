package com.lmy.codec.texture.impl

import android.opengl.GLES20
import com.lmy.codec.texture.Texture
import java.nio.FloatBuffer

/**
 * Created by lmyooyo@gmail.com on 2018/3/27.
 */
open abstract class BaseTexture(var buffer: FloatBuffer? = null,
                                var vertexShader: Int? = null,
                                var fragmentShader: Int? = null,
                                var mShaderProgram: Int? = null) : Texture {
    /**
     * 加载着色器，GL_VERTEX_SHADER代表生成顶点着色器，GL_FRAGMENT_SHADER代表生成片段着色器
     */
    fun loadShader(type: Int, shaderSource: String): Int {
        //创建Shader
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) {
            throw RuntimeException("Create Shader Failed!" + GLES20.glGetError())
        }
        //加载Shader代码
        GLES20.glShaderSource(shader, shaderSource)
        //编译Shader
        GLES20.glCompileShader(shader)
        return shader
    }

    /**
     * 将两个Shader链接至program中
     */
    fun linkProgram(verShader: Int, fragShader: Int): Int {
        //创建program
        val program = GLES20.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Create Program Failed!" + GLES20.glGetError())
        }
        //附着顶点和片段着色器
        GLES20.glAttachShader(program, verShader)
        GLES20.glAttachShader(program, fragShader)
        //链接program
        GLES20.glLinkProgram(program)
        //告诉OpenGL ES使用此program
        GLES20.glUseProgram(program)
        return program
    }

    fun getAttribLocation(name: String): Int {
        return GLES20.glGetAttribLocation(mShaderProgram!!, name)
    }

    fun getUniformLocation(name: String): Int {
        return GLES20.glGetUniformLocation(mShaderProgram!!, name)
    }
}