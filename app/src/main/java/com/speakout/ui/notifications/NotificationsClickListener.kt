package com.speakout.ui.notifications

import android.widget.ImageView
import com.speakout.notification.NotificationResponse

interface NotificationsClickListener {
    fun onPostClick(notification: NotificationResponse)
    fun onProfileClick(notification: NotificationResponse, imageView: ImageView)
}