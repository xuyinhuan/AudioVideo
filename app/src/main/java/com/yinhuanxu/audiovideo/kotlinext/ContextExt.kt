package com.yinhuanxu.audiovideo.kotlinext

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Activity.verifyStoragePermissions() {
    try {
        val permission = ActivityCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE")
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf("android.permission.READ_EXTERNAL_STORAGE",
                            "android.permission.WRITE_EXTERNAL_STORAGE"),
                    1)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.log(info: String) {
    Log.i(this::class.java.simpleName, info)
}