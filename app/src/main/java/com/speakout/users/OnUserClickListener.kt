package com.speakout.users

import android.widget.ImageView
import com.speakout.auth.UsersItem

interface OnUserClickListener {
    fun onUserClick(userMiniDetails: UsersItem, profileImageView: ImageView)
    fun onFollowClick(userMiniDetails: UsersItem)
    fun onUnFollowClick(userMiniDetails: UsersItem)
}