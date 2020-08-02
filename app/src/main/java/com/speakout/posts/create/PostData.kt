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
    var timeStamp: Long = 0,
    var username: String = "",
    var userImageUrl: String = "",
    var likesCount: Long = 0,
    var isLikedBySelf: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readLong() ?: 0,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong() ?: 0,
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(userId)
        parcel.writeString(content)
        parcel.writeStringList(tags)
        parcel.writeString(postImageUrl)
        parcel.writeLong(timeStamp)
        parcel.writeString(username)
        parcel.writeString(userImageUrl)
        parcel.writeLong(likesCount)
        parcel.writeByte(if (isLikedBySelf) 1 else 0)
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
}
