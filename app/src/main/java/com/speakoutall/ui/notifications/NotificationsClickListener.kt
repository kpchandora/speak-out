package com.speakoutall.ui.notifications

import android.widget.ImageView
import com.speakoutall.notification.NotificationsItem

interface NotificationsClickListener {
    fun onPostClick(notification: NotificationsItem)
    fun onProfileClick(notification: NotificationsItem, imageView: ImageView)
}