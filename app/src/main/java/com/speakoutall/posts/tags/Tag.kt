package com.speakoutall.posts.tags

import com.google.gson.annotations.Expose

data class Tag(
    val id: Long = 0,
    val tag: String = "",
    var used: Long? = 0
) {
    @Expose(deserialize = false, serialize = false)
    var uploading: Boolean? = false
}