package com.speakoutall.ui.search

import android.widget.ImageView
import com.speakoutall.auth.UsersItem

interface OnSearchUserClickListener {
    fun onUserClick(userMiniDetails: UsersItem, profileImageView: ImageView)
}