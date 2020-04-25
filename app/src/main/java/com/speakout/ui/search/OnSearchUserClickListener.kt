package com.speakout.ui.search

import android.widget.ImageView
import com.speakout.auth.UserMiniDetails

interface OnSearchUserClickListener {
    fun onUserClick(userMiniDetails: UserMiniDetails, profileImageView: ImageView)
}