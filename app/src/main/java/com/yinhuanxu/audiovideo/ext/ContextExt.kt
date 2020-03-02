package com.yinhuanxu.audiovideo.ext

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.yinhuanxu.audiovideo.MainActivity

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.dp2px(dp: Float): Int {
    val scale = resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

fun Activity.verifyStoragePermissions() {
    try {
        //检测是否有写的权限
        val permission = ActivityCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE")

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
                    , 1);
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun logI(info: String) {
    Log.i(MainActivity.TAG, info)
}