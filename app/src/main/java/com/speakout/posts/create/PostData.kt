package com.speakout.posts.create

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude

data class PostData(
    var postId: String = "",
    var userId: String = "",
    var content: String = "",
    var tags: List<String> = emptyList(),
    var postImageUrl: String = "",
    var timeStamp: String = "",
    var username: String = "",
    var userImageUrl: String = "",
    var likes: List<String> = emptyList(),
    var timeStampLong: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(userId)
        parcel.writeString(content)
        parcel.writeStringList(tags)
        parcel.writeString(postImageUrl)
        parcel.writeString(timeStamp)
        parcel.writeString(username)
        parcel.writeString(userImageUrl)
        parcel.writeStringList(likes)
        parcel.writeLong(timeStampLong)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostData> {
        override fun createFromParcel(parcel: Parcel): PostData {
            return PostData(parcel)
        }

        override fun newArray(size: Int): Array<PostData?> {
            return arrayOfNulls(size)
        }
    }

    @set:Exclude
    @get:Exclude
    var likesSet = hashSetOf<String>()

}