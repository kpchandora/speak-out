package com.speakout.users

import android.widget.ImageView
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails

interface OnUserClickListener {
    fun onUserClick(userMiniDetails: UserMiniDetails, profileImageView: ImageView)
    fun onFollowClick(userMiniDetails: UserMiniDetails)
    fun onUnFollowClick(userMiniDetails: UserMiniDetails)
}