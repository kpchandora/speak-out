package com.speakoutall.ui.profile

import android.widget.ImageView
import com.speakoutall.posts.create.PostData

interface ProfilePostClickListener {
    fun onPostClick(postData: PostData, postImageView: ImageView, position: Int)
}