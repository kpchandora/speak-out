package com.speakout.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.widget.Toast

fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun <T> Context.openActivity(clazz: Class<T>): Intent {
    return Intent(this, clazz).also {
        startActivity(it)
    }
}

fun Activity.getScreenSize(): DisplayMetrics{
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}
