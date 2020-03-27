package com.speakout.ui.home

import com.speakout.posts.create.PostData

interface PostClickEventListener {
    fun onLike(position: Int, postData: PostData)
    fun onDislike(position: Int, postData: PostData)
    fun onProfileClick(postData: PostData)
}