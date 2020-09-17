package com.speakout.ui.notifications

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.gone
import com.speakout.extensions.loadImage
import com.speakout.extensions.visible
import com.speakout.notification.NotificationResponse
import kotlinx.android.synthetic.main.item_notification_layout.view.*

class NotificationsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(notification: NotificationResponse) {
        with(view) {
            iv_notification_profile.loadImage(
                url = notification.photoUrl,
                placeholder = R.drawable.ic_account_circle_grey,
                makeRound = true
            )

            tv_time.text = notification.timestamp.toString()
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

        }
    }

    private fun getSpannable(res: Int, username: String): Spanned {
        val content = view.context.getString(res, username)
        return HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

}