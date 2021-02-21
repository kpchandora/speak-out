package com.speakout.ui.search

import android.widget.ImageView
import com.speakout.auth.UsersItem

interface OnSearchUserClickListener {
    fun onUserClick(userMiniDetails: UsersItem, profileImageView: ImageView)
}