package com.yinhuanxu.audiovideo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class FFmpegActivity : AppCompatActivity() {

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ffmpeg)

    }
}