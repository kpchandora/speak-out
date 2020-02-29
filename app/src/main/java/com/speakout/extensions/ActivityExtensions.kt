package com.speakout.extensions

import android.content.Context
import android.content.Intent
import android.widget.Toast

fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun <T> Context.openActivity(clazz: Class<T>): Intent {
    return Intent(this, clazz).also {
        startActivity(it)
    }
}

