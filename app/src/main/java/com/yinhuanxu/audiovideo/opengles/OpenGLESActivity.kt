package com.yinhuanxu.audiovideo.opengles

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yinhuanxu.audiovideo.R
import kotlinx.android.synthetic.main.activity_opengles.*

class OpenGLESActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opengles)
        initGLSurfaceView()
    }

    private fun initGLSurfaceView() {
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(TriangleRenderer())
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}