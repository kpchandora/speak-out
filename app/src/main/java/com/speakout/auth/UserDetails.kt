package com.speakout.auth

import android.os.Parcel
import android.os.Parcelable
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
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong()
    ) {
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<UserDetails> {
            override fun createFromParcel(parcel: Parcel): UserDetails {
                return UserDetails(parcel)
            }

            override fun newArray(size: Int): Array<UserDetails?> {
                return arrayOfNulls(size)
            }
        }

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(phoneNumber)
        parcel.writeString(photoUrl)
        parcel.writeValue(creationTimeStamp)
        parcel.writeValue(lastSignInTimestamp)
        parcel.writeString(username)
        parcel.writeValue(lastUpdated)
        parcel.writeLong(postsCount)
        parcel.writeLong(followersCount)
        parcel.writeLong(followingsCount)
    }

    override fun describeContents(): Int {
        return 0
    }


}
