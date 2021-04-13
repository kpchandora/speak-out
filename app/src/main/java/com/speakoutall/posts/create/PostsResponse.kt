package com.speakoutall.posts.create

import android.os.Parcel
import android.os.Parcelable

data class PostsResponse(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val key: Long = -1,
    val posts: List<PostData> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.createTypedArrayList(PostData) ?: emptyList()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(pageNumber)
        parcel.writeInt(pageSize)
        parcel.writeLong(key)
        parcel.writeTypedList(posts)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostsResponse> {
        override fun createFromParcel(parcel: Parcel): PostsResponse {
            return PostsResponse(parcel)
        }

        override fun newArray(size: Int): Array<PostsResponse?> {
            return arrayOfNulls(size)
        }
    }
}

data class PostData(
    var postId: String = "",
    var userId: String = "",
    var content: String = "",
    var tags: List<String> = ArrayList(),
    var postImageUrl: String = "",
    var timeStamp: Long = 0,
    var username: String = "",
    var photoUrl: String = "",
    var likesCount: Long = 0,
    var isLikedBySelf: Boolean = false,
    var isBookmarkedBySelf: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: ArrayList(),
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
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
        parcel.writeString(photoUrl)
        parcel.writeLong(likesCount)
        parcel.writeByte(if (isLikedBySelf) 1 else 0)
        parcel.writeByte(if (isBookmarkedBySelf) 1 else 0)
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