package com.yinhuanxu.audiovideo.kotlinext

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.log(info: String) {
    Log.i(this::class.java.simpleName, info)
}