package com.speakout.notification


import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("photoUrl")
    val photoUrl: String?,

    @SerializedName("postId")
    val postId: String?,

    @SerializedName("postImageUrl")
    val postImageUrl: String?,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("type")
    val type: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("username")
    val username: String
)