package com.speakout.extensions

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast

fun Activity.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun <T> Activity.openActivity(clazz: Class<T>): Intent {
    return Intent(this, clazz).also {
        startActivity(it)
    }
}

fun Activity.visible(view: View) {
    view.visibility = View.VISIBLE
}

fun Activity.gone(view: View) {
    view.visibility = View.GONE
}