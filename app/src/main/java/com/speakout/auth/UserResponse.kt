package com.speakout.auth

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserResponse(

    @field:SerializedName("pageNumber")
    val key: Long = 0,

    @field:SerializedName("pageSize")
    val pageSize: Int = 0,

    @field:SerializedName("users")
    val users: List<UsersItem> = emptyList()
) : Parcelable

@Parcelize
data class UsersItem(

    @field:SerializedName("photoUrl")
    val photoUrl: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("isFollowedBySelf")
    var isFollowedBySelf: Boolean? = null,

    @field:SerializedName("userId")
    val userId: String = "",

    @field:SerializedName("username")
    val username: String? = null,

    @field:SerializedName("phoneNumber")
    val phoneNumber: String? = null
) : Parcelable
