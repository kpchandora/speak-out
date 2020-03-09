package com.speakout.extensions

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import androidx.core.content.ContextCompat

fun View.visible() {
    this.visibility = View.VISIBLE
}


fun View.gone() {
    this.visibility = View.GONE
}

fun View.addViewObserver(function: () -> Unit) {
    val view = this
    view.viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            function.invoke()
        }
    })
}

fun EditText.setDrawableEnd(drawable: Int) {
    setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
}

fun EditText.removeDrawableEnd() {
    setDrawableEnd(0)
}

