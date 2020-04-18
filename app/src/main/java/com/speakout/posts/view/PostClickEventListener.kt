package com.speakout.posts.view

import android.widget.ImageView
import com.speakout.posts.create.PostData

interface PostClickEventListener {
    fun onLike(position: Int, postData: PostData)
    fun onDislike(position: Int, postData: PostData)
    fun onProfileClick(postData: PostData, profileImageView: ImageView)
    fun onLikedUsersClick(postData: PostData)
    fun onMenuClick(postData: PostData, position: Int)
}