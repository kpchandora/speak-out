package com.speakoutall.users

import android.widget.ImageView
import com.speakoutall.auth.UsersItem

interface OnUserClickListener {
    fun onUserClick(userMiniDetails: UsersItem, profileImageView: ImageView)
    fun onFollowClick(userMiniDetails: UsersItem)
    fun onUnFollowClick(userMiniDetails: UsersItem)
}