package com.speakoutall.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.notification.NotificationsItem

class NotificationsAdapter(
    private val notifications: ArrayList<NotificationsItem>,
    private val listener: NotificationsClickListener
) :
    RecyclerView.Adapter<NotificationsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_layout, parent, false)
        return NotificationsViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size
}