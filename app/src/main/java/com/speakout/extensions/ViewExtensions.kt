package com.speakout.extensions

import android.graphics.drawable.Drawable
import android.text.Editable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout

fun View.visible() {
    this.visibility = View.VISIBLE
}


fun View.gone() {
    this.visibility = View.GONE
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

inline fun View.addViewObserver(crossinline function: () -> Unit) {
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

fun TextInputLayout.checkAndShowError(text: Editable?, error: String): Boolean {
    text?.let {
        if (it.toString().trim().isNotEmpty()) {
            this.error = null
            return true
        }
    }
    this.error = error
    return false
}

