package com.speakout.common

sealed class Result<out T> {
    data class Success<out A>(val data: A) : Result<A>()
    data class Error<A>(val error: Exception, val data: A? = null) : Result<A>()
}