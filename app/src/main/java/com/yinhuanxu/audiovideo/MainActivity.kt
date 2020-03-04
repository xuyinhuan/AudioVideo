package com.yinhuanxu.audiovideo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.yinhuanxu.audiovideo.kotlinext.verifyStoragePermissions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        verifyStoragePermissions()
        initClickListener()
    }

    private fun initClickListener() {
        androidMedia.setOnClickListener {
            val intent = Intent(this, AndroidMediaActivity::class.java)
            startActivity(intent)
        }

        ffmpeg.setOnClickListener {
            val intent = Intent(this, FFmpegActivity::class.java)
            startActivity(intent)
        }
    }

}
