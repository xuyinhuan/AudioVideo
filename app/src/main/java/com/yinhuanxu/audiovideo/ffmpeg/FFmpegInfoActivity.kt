package com.yinhuanxu.audiovideo.ffmpeg

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yinhuanxu.audiovideo.R
import kotlinx.android.synthetic.main.activity_ffmpeg_info.*

class FFmpegInfoActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ffmpeg_info)
        initClickListener()
    }

    private fun initClickListener() {
        protocol.setOnClickListener {
            info.text = getProtocolInfo()
        }

        avFormat.setOnClickListener {
            info.text = getAVFormatInfo()
        }

        avCodec.setOnClickListener {
            info.text = getAVCodecInfo()
        }

        avFilter.setOnClickListener {
            info.text = getAVFilterInfo()
        }

        configure.setOnClickListener {
            info.text = getConfigurationInfo()
        }
    }

    private external fun getProtocolInfo(): String
    private external fun getAVFormatInfo(): String
    private external fun getAVCodecInfo(): String
    private external fun getAVFilterInfo(): String
    private external fun getConfigurationInfo(): String
}