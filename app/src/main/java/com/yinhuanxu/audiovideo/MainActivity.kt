package com.yinhuanxu.audiovideo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yinhuanxu.audiovideo.ffmpeg.FFmpegActivity
import com.yinhuanxu.audiovideo.webrtc.CallActivity
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initClickListener()
        requestPermissions()
    }

    private fun requestPermissions() {
        val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(this,
                    "Need permissions for camera & microphone", 0, *perms)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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

        webrtc.setOnClickListener {
            val intent = Intent(this, CallActivity::class.java)
            startActivity(intent)
        }
    }

}
