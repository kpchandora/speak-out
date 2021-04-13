package com.speakoutall.ui.notifications

import android.text.Spanned
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.loadImage
import com.speakoutall.extensions.toFormattedTime
import com.speakoutall.extensions.visible
import com.speakoutall.notification.NotificationsItem
import kotlinx.android.synthetic.main.item_notification_layout.view.*

class NotificationsViewHolder(
    private val view: View,
    private val listener: NotificationsClickListener
) :
    RecyclerView.ViewHolder(view) {

    fun bind(notification: NotificationsItem) {
        with(view) {
            iv_notification_profile.loadImage(
                url = notification.photoUrl,
                placeholder = R.drawable.ic_account_circle_grey,
                makeRound = true
            )

            tv_time.text = notification.timestamp.toFormattedTime()

            if (notification.type == "follow") {
                iv_notification_post.gone()
                tv_notification_content.text =
                    getSpannable(R.string.notification_follow, notification.username)
            } else {
                iv_notification_post.visible()
                iv_notification_post.loadImage(
                    url = notification.postImageUrl,
                    placeholder = R.color.black
                )
                tv_notification_content.text =
                    getSpannable(R.string.like_post, notification.username)
            }

            iv_notification_profile.transitionName = notification.timestamp.toString()
            iv_notification_profile.setOnClickListener {
                listener.onProfileClick(notification, iv_notification_profile)
            }

            iv_notification_post.setOnClickListener {
                listener.onPostClick(notification)
            }

        }
    }

    private fun getSpannable(res: Int, username: String): Spanned {
        val content = view.context.getString(res, username)
        return HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

}