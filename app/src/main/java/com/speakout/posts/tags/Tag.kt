package com.speakout.posts.tags

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.Expose

@IgnoreExtraProperties
data class Tag(
    val id: Long = 0,
    val tag: String = "",
    var used: Long? = 0
) {
    @Expose(deserialize = false, serialize = false)
    var uploading: Boolean? = false
}