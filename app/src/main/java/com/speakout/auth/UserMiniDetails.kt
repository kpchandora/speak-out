package com.speakout.auth

data class UserMiniDetails(
    val userId: String = "",
    val name: String? = null,
    val photoUrl: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
    val isFollowedBySelf: Boolean? = null
)