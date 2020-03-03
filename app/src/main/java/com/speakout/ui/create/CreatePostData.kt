package com.speakout.ui.create

data class CreatePostData(
    val postId: String,
    val userId: String,
    val content: String,
    val tagsList: List<String>,
    val likesCount: List<String>,
    val postImageUrl: String,
    val timeStamp: String
)