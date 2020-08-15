package com.speakout.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        const val REMOVE_LIKE = "remove_like"
        const val NOTIFICATION_ID = "notificationId"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("Data: ${remoteMessage.data}")
        Timber.d("Thread: ${Looper.myLooper() == Looper.getMainLooper()}")
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

                    }
                    REMOVE_LIKE -> {

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

        val channelId = "speak_out"
        val customNotification = NotificationCompat.Builder(this, "speak_out")
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//            .setContentTitle("SpeakOut")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText("${map["username"]} started following you.")
//            .setCustomContentView(notificationLayout)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "notification_follow",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify("${map[NOTIFICATION_ID]}", 0, customNotification)

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