package com.speakout.posts.tags

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Tag(
    val id: Long = 0,
    val tag: String = "",
    var used: Long? = 0
) {
    @get:Exclude
    @set:Exclude
    var uploading: Boolean? = false

}