package com.speakout.extensions

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber
import java.lang.Exception

fun View.visible() {
    this.visibility = View.VISIBLE
}


fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
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


fun ImageView.loadImage(url: String?, placeholder: Int, makeRound: Boolean = false) {
    var glide = if (url.isNotNullOrEmpty()) {
        Glide.with(this).load(url)
    } else {
        Glide.with(this).load(placeholder)
    }

    if (makeRound) {
        glide = glide.apply(RequestOptions.circleCropTransform())
    }
    glide
        .thumbnail(.1f)
        .placeholder(placeholder)
        .error(placeholder)
        .into(this)

}

inline fun ImageView.loadImageWithCallback(
    url: String, makeRound: Boolean = false,
    crossinline onSuccess: () -> Unit,
    crossinline onFailed: () -> Unit
) {
    var glide = Glide.with(this)
        .load(url)

    if (makeRound) {
        glide = glide.apply(RequestOptions.circleCropTransform())
    }

    glide.listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            onFailed.invoke()
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            onSuccess.invoke()
            return false
        }
    })
        .thumbnail(.1f)
        .into(this)
}

fun EditText.setSmallCaseFilter() {
    val filter = InputFilter { source, _, _, _, _, _ ->
        try {
            source[0].let {
                return@InputFilter it.toLowerCase().toString()
            }
        } catch (e: Exception) {
        }
        null
    }
    filters = arrayOf(filter)
}

