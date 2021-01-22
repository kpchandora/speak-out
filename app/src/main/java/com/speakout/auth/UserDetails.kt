package com.speakout.auth

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDetails(
    val userId: String = "",
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val creationTimeStamp: Long? = null,
    val lastSignInTimestamp: Long? = null,
    val username: String? = null,
    val lastUpdatedAt: Long? = 0,
    val postsCount: Long = 0,
    val followersCount: Long = 0,
    val followingsCount: Long = 0,
    val isFollowedBySelf: Boolean = false
) : Parcelable