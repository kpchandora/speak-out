package com.speakout.extensions

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