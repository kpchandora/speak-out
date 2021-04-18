package com.speakoutall.notification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationResponse(

    @field:SerializedName("key")
    val key: Long = 0,

    @field:SerializedName("pageSize")
    val pageSize: Int = 0,

    @field:SerializedName("notifications")
    val notifications: List<NotificationsItem> = emptyList()
) : Parcelable

@Parcelize
data class NotificationsItem(

    @field:SerializedName("photoUrl")
    val photoUrl: String?,

    @field:SerializedName("postId")
    val postId: String?,

    @field:SerializedName("postImageUrl")
    val postImageUrl: String?,

    @field:SerializedName("type")
    val type: String?,

    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("timestamp")
    val timestamp: Long,

    @field:SerializedName("username")
    val username: String
) : Parcelable
