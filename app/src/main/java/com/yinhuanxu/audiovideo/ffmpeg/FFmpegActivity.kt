package com.yinhuanxu.audiovideo.ffmpeg

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yinhuanxu.audiovideo.R

import kotlinx.android.synthetic.main.activity_ffmpeg.*

class FFmpegActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ffmpeg)
        initClickListener()
    }

    private fun initClickListener() {
        ffmpegInfo.setOnClickListener {
            val intent = Intent(this, FFmpegInfoActivity::class.java)
            startActivity(intent)
        }
    }
}