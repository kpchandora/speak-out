package com.speakout.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.speakout.R

fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun <T> Context.openActivity(clazz: Class<T>): Intent {
    return Intent(this, clazz).also {
        startActivity(it)
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