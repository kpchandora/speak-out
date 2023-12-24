package com.speakoutall.ui.notifications

import android.text.Spanned
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.databinding.ItemNotificationLayoutBinding
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.loadImage
import com.speakoutall.extensions.toFormattedTime
import com.speakoutall.extensions.visible
import com.speakoutall.notification.NotificationsItem

class NotificationsViewHolder(
    private val binding: ItemNotificationLayoutBinding,
    private val listener: NotificationsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(notification: NotificationsItem) {
        binding.ivNotificationProfile.loadImage(
            url = notification.photoUrl,
            placeholder = R.drawable.ic_account_circle_grey,
            makeRound = true
        )

        binding.tvTime.text = notification.timestamp.toFormattedTime()

        if (notification.type == "follow") {
            binding.ivNotificationPost.gone()
            binding.tvNotificationContent.text =
                getSpannable(R.string.notification_follow, notification.username)
        } else {
            binding.ivNotificationPost.visible()
            binding.ivNotificationPost.loadImage(
                url = notification.postImageUrl,
                placeholder = R.color.black
            )
            binding.tvNotificationContent.text =
                getSpannable(R.string.like_post, notification.username)
        }

        binding.ivNotificationProfile.transitionName = notification.timestamp.toString()
        binding.ivNotificationProfile.setOnClickListener {
            listener.onProfileClick(notification, binding.ivNotificationProfile)
        }

        binding.ivNotificationPost.setOnClickListener {
            listener.onPostClick(notification)
        }
    }

    private fun getSpannable(res: Int, username: String): Spanned {
        val content = binding.root.context.getString(res, username)
        return HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

}