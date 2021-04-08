package com.speakout.ui.notifications

import android.widget.ImageView
import com.speakout.notification.NotificationsItem

interface NotificationsClickListener {
    fun onPostClick(notification: NotificationsItem)
    fun onProfileClick(notification: NotificationsItem, imageView: ImageView)
}