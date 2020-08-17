package com.speakout.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.extensions.isNotNullOrEmpty
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.net.URL

/**
 * Created by Kalpesh on 09/08/20.
 */
public class SpeakOutFirebaseMessagingService : FirebaseMessagingService() {

    private val userRepository by lazy {
        UsersRepository(RetrofitBuilder.apiService, AppPreference)
    }

    companion object {
        const val FOLLOW = "follow"
        const val UNFOLLOW = "unfollow"
        const val LIKE = "like"
        const val REMOVE_LIKE = "removeLike"
        const val NOTIFICATION_ID = "notificationId"
        const val USER_ID = "userId"
        const val POST_IMAGE_URL = "postImageUrl"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("Data: ${remoteMessage.data}")
        remoteMessage.data.let {
            val type = it["type"]
            if (type.isNotNullOrEmpty()) {
                when (type) {
                    FOLLOW -> {
                        sendFollowNotification(it)
                    }
                    UNFOLLOW -> {
                        removeNotificationIfPresent(it)
                    }
                    LIKE -> {
                        sendLikeNotification(it)
                    }
                }
            }
        }
    }

    private fun sendFollowNotification(map: MutableMap<String, String>) {
        val bundle = Bundle()
        val notificationLayout = RemoteViews(packageName, R.layout.layout_custom_notification)
        notificationLayout.setTextViewText(
            R.id.tv_notification_desc,
            "${map["username"]} started following you."
        )

        val channelId = "new_followers"
        val customNotification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//            .setContentTitle("SpeakOut")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle("${map["username"]} started following you.")
//            .setCustomContentView(notificationLayout)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New Followers",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify("${map[NOTIFICATION_ID]}", 0, customNotification)

        Timber.d("Notification sent")
    }

    private fun sendLikeNotification(map: MutableMap<String, String>) {
        val notificationLayout = RemoteViews(packageName, R.layout.layout_custom_notification)
        notificationLayout.setTextViewText(
            R.id.tv_notification_desc,
            "${map["username"]}(${map["name"]}) liked your post."
        )

        val channelId = "likes"
        val customNotification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//            .setContentTitle("SpeakOut")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText("${map["username"]}(${map["name"]}) liked your post.")
//            .setCustomContentView(notificationLayout)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.indigo_500))

        try {
            map[POST_IMAGE_URL]?.let {
                val bitmap = BitmapFactory.decodeStream(URL(it).openConnection().getInputStream())
                if (bitmap != null) {
                    customNotification.setLargeIcon(bitmap)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Likes",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setShowBadge(true)
            channel.importance = NotificationManager.IMPORTANCE_HIGH
            manager.createNotificationChannel(channel)
        }

        manager.notify("${map[NOTIFICATION_ID]}", 0, customNotification.build())

        Timber.d("Notification sent")
    }

    private fun removeNotificationIfPresent(map: MutableMap<String, String>) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel("${map[NOTIFICATION_ID]}", 0)
    }

    override fun onNewToken(token: String) {
        Timber.d("Token: $token")
        GlobalScope.launch {
            userRepository.updateFcmToken(token)
        }
    }
}