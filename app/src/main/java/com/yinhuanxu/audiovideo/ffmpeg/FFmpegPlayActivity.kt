package com.yinhuanxu.audiovideo.ffmpeg

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import com.yinhuanxu.audiovideo.R

import kotlinx.android.synthetic.main.activity_ffmpeg_play.*

class FFmpegPlayActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    private val url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ffmpeg_play)
        initClickListener()
    }

    private fun initClickListener() {
        playLocal.setOnClickListener {
            Thread {
                val videoPath = "${Environment.getExternalStorageDirectory()}/input.mp4"
                playLocalVideo(videoPath, surfaceView.holder.surface)
            }.start()
        }

        playRemote.setOnClickListener {
            Thread {
                playRemoteVideo(url, surfaceView.holder.surface)
            }.start()
        }
    }

    private external fun playLocalVideo(path: String, surface: Surface)

    private external fun playRemoteVideo(path: String, surface: Surface)

}