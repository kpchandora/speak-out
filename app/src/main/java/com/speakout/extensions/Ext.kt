package com.speakout.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.speakout.utils.TimeUtils

fun CharSequence?.isNotNullOrEmpty(): Boolean {
    return this != null && this.trim().isNotEmpty()
}

fun Long.toFormattedTime(): String {
    return TimeUtils.getFormattedTimeWithCharSuffix(this)
}

fun Long.toElapsedTime(): String{
    return TimeUtils.getFormattedElapsedTime(this)
}

fun <T: ViewModel> T.createFactory(): ViewModelProvider.Factory {
    val viewModel = this
    return object: ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T: ViewModel> create(modelClass: Class<T>): T = viewModel as T
    }
}