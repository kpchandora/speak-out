package com.speakout.ui.profile

import android.widget.ImageView
import com.speakout.posts.create.PostData

interface ProfilePostClickListener {
    fun onPostClick(postData: PostData, postImageView: ImageView)
}