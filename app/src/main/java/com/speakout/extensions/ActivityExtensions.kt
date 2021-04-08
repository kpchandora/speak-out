package com.speakout.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.speakout.R

fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showShortToast(message: String) {
    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
}


fun Activity.getScreenSize(): DisplayMetrics {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

fun Fragment.setUpToolbar(view: View): Toolbar? {
    view.findViewById<Toolbar>(R.id.toolbar)?.let {
        val navHostFragment = NavHostFragment.findNavController(this)
        NavigationUI.setupWithNavController(it, navHostFragment)
        return it
    }
    return null
}

fun Fragment.setUpWithAppBarConfiguration(view: View, fragmentId: Int? = null): Toolbar? {
    view.findViewById<Toolbar>(R.id.toolbar)?.let {
        val set = mutableSetOf(
            R.id.navigation_home,
            R.id.navigation_search,
            R.id.navigation_new_post,
            R.id.notificationFragment,
            R.id.navigation_profile
        )
        fragmentId?.let { id ->
            set.add(id)
        }
        val appBarConfiguration = AppBarConfiguration(set)

        val navHostFragment = NavHostFragment.findNavController(this)
        NavigationUI.setupWithNavController(it, navHostFragment, appBarConfiguration)
        return it
    }
    return null
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
