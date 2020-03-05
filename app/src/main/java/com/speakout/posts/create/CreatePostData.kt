package com.speakout.posts.create

import android.os.Parcel
import android.os.Parcelable

data class CreatePostData(
    var postId: String = "",
    var userId: String = "",
    var content: String = "",
    var tags: List<String> = emptyList(),
    var postImageUrl: String = "",
    var timeStamp: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(userId)
        parcel.writeString(content)
        parcel.writeStringList(tags)
        parcel.writeString(postImageUrl)
        parcel.writeString(timeStamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CreatePostData> {
        override fun createFromParcel(parcel: Parcel): CreatePostData {
            return CreatePostData(parcel)
        }

        override fun newArray(size: Int): Array<CreatePostData?> {
            return arrayOfNulls(size)
        }
    }
}