package com.speakout.auth

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class UserDetails(
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

    @PropertyName("user_name")
    val username: String? = null
) {

    companion object {
        @Exclude
        fun usernameMap(username: String) = mapOf(
            "user_name" to username
        )

        @Exclude
        fun updateName(name: String) = mapOf(
            "name" to name
        )

        @Exclude
        fun updateNumber(number: String) = mapOf(
            "phoneNumber" to number
        )

        @Exclude
        fun updatePhoto(photoUrl: String) = mapOf(
            "photoUrl" to photoUrl
        )
    }

}