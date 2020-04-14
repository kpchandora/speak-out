package com.speakout.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.speakout.R
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.item_post_layout.view.*
import java.lang.Exception

fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showShortToast(message: String) {
    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
}

fun <T> Context.openActivity(clazz: Class<T>, extras: Bundle? = null): Intent {
    return Intent(this, clazz).also { intent ->
        extras?.let {
            intent.putExtras(extras)
        }
        startActivity(intent)
    }
}

fun <T> Activity.openActivityForResult(
    clazz: Class<T>,
    requestCode: Int,
    extras: Bundle? = null
): Intent {
    return Intent(this, clazz).also { intent ->
        extras?.let {
            intent.putExtras(extras)
        }
        startActivityForResult(intent, requestCode)
    }
}

fun Activity.getScreenSize(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

fun AppCompatActivity.addFragment(
    container: Int,
    fragment: Fragment,
    tag: Int = 0,
    backStackTag: String? = null
) {
    supportFragmentManager.beginTransaction().let {
        it.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
        it.add(container, fragment)
        it.addToBackStack(backStackTag)
        it.commit()
    }
}


fun Activity.showKeyboard(editText: EditText? = null) {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.also {
        editText?.let { et ->
            it.showSoftInput(et, InputMethodManager.SHOW_FORCED)
        } ?: it.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}

fun Activity.hideKeyboard(editText: EditText? = null) {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.also {
        editText?.let { et ->
            it.hideSoftInputFromWindow(et.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
        } ?: it.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}
