package com.yinhuanxu.audiovideo.webrtc

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.yinhuanxu.audiovideo.R
import com.yinhuanxu.audiovideo.kotlinext.showToast
import kotlinx.android.synthetic.main.activity_rtc_call.*

class CallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtc_call)
        joinRoomBtn.setOnClickListener {
            val addr = serverEditText.text.toString()
            val roomName = roomEditText.text.toString()
            if (!TextUtils.isEmpty(roomName)) {
                val intent = Intent(this@CallActivity, RoomActivity::class.java)
                intent.putExtra("ServerAddr", addr)
                intent.putExtra("RoomName", roomName)
                startActivity(intent)
            } else {
                showToast("room name is empty")
            }
        }
    }
}