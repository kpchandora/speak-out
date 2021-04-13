package com.speakoutall.posts.view

import android.widget.ImageView
import com.speakoutall.posts.create.PostData

interface PostClickEventListener {
    fun onLike(position: Int, postData: PostData)
    fun onRemoveLike(position: Int, postData: PostData)
    fun onProfileClick(postData: PostData, profileImageView: ImageView)
    fun onLikedUsersClick(postData: PostData)
    fun onMenuClick(postData: PostData, position: Int)
    fun onBookmarkAdd(postData: PostData)
    fun onBookmarkRemove(postId: String)
}