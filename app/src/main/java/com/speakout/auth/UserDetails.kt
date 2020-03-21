package com.speakout.auth

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class UserDetails(

    val userId: String = "",

    @PropertyName("name")
    val name: String? = null,

    @PropertyName("email")
    val email: String? = null,

    @PropertyName("phoneNumber")
    val phoneNumber: String? = null,

    @PropertyName("photoUrl")
    val photoUrl: String? = null,

    @PropertyName("creationTimeStamp")
    val creationTimeStamp: Long? = null,

    @PropertyName("lastSignInTimestamp")
    val lastSignInTimestamp: Long? = null,

    @PropertyName("username")
    val username: String? = null,

    val lastUpdated: Long? = 0,

    val postsCount: Long = 0,

    val followersCount: Long = 0,

    val followingsCount: Long = 0
) {

    companion object {
        @Exclude
        fun updateUsername(username: String) = Pair("username", username)

        @Exclude
        fun updateName(name: String) = Pair("name", name)

        @Exclude
        fun updateNumber(number: String) = Pair("phoneNumber", number)

        @Exclude
        fun updatePhoto(photoUrl: String) = Pair("photoUrl", photoUrl)

        @Exclude
        fun updateTimeStamp(time: Long) = Pair("lastUpdated", time)
    }

}