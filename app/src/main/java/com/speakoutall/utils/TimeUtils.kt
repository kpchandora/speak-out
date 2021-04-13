package com.speakoutall.utils

import com.speakoutall.R
import com.speakoutall.SpeakOutApp

object TimeUtils {

    private const val oneSecond = 1000L // 1 Second = 1000 milli-seconds
    private const val oneMinute = oneSecond * 60 // 1 minutes = 60 seconds
    private const val oneHour = oneMinute * 60 // 1 hour = 60 minutes
    private const val oneDay = oneHour * 24 // 1 day = 24 hours
    private const val oneWeek = oneDay * 7 // 1 Week = 7 days
    private const val oneMonth = oneDay * 30 // 1 month = 30 days
    private const val oneYear = oneMonth * 12 // 1 year = 12 months

    fun getFormattedTimeWithCharSuffix(time: Long): String {
        val pair = getCorrectTimeFormat(System.currentTimeMillis() - time)
        val timeType = when (pair.first) {
            Time.SECONDS -> "s"
            Time.MINUTES -> "m"
            Time.HOURS -> "h"
            Time.DAYS -> "d"
            Time.WEEKS -> "w"
            Time.MONTHS -> " months"
            Time.YEARS -> " years"
        }
        return "${pair.second}$timeType"
    }

    fun getFormattedElapsedTime(time: Long): String {
        val context = SpeakOutApp.appContext!!
        val pair = getCorrectTimeFormat(System.currentTimeMillis() - time)
        val id = when (pair.first) {
            Time.SECONDS -> R.plurals.plural_second
            Time.MINUTES -> R.plurals.plural_minute
            Time.HOURS -> R.plurals.plural_hour
            Time.DAYS -> R.plurals.plural_day
            Time.WEEKS -> R.plurals.plural_week
            Time.MONTHS -> R.plurals.plural_month
            Time.YEARS -> R.plurals.plural_year
        }
        return context.resources.getQuantityString(id, pair.second.toInt(), pair.second.toInt())
    }

    private fun getCorrectTimeFormat(timeDiff: Long): Pair<Time, Long> {
        return when {
            timeDiff > oneYear -> Pair(Time.YEARS, timeDiff / oneYear)
            timeDiff > oneMonth -> Pair(Time.MONTHS, timeDiff / oneMonth)
            timeDiff > oneWeek -> Pair(Time.WEEKS, timeDiff / oneWeek)
            timeDiff > oneDay -> Pair(Time.DAYS, timeDiff / oneDay)
            timeDiff > oneHour -> Pair(Time.HOURS, timeDiff / oneHour)
            timeDiff > oneMinute -> Pair(Time.MINUTES, timeDiff / oneMinute)
            else -> Pair(Time.SECONDS, timeDiff / oneSecond)
        }
    }
}

enum class Time {
    SECONDS,
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS
}