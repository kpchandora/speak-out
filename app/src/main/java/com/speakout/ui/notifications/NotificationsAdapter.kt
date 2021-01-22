package com.speakout.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.notification.NotificationResponse

class NotificationsAdapter(private val listener: NotificationsClickListener) :
    RecyclerView.Adapter<NotificationsViewHolder>() {

    private val notifications = ArrayList<NotificationResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_layout, parent, false)
        return NotificationsViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    fun updateData(list: List<NotificationResponse>) {
        notifications.clear()
        notifications.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = notifications.size
}