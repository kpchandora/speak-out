package com.speakout.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Bundle
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
        const val NOTIFICATION_ID = "notificationId"
        const val USER_ID = "userId"
        const val OTHER = "other"
        const val USER_IMAGE_URL = "userImageUrl"
    }

    override fun onNewToken(token: String) {
        Timber.d("Token: $token")
        GlobalScope.launch {
            userRepository.updateFcmToken(token)
        }
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
                    OTHER -> {
                        //This is for other notifications such as some events
                    }
                }
            }
        }
    }

    private fun sendFollowNotification(map: MutableMap<String, String>) {
        val bundle = Bundle()

        val channelId = "new_followers"
        val notification = getNotificationBuilder(channelId = channelId)
        notification.setContentTitle("${map["username"]} started following you.")

        getUserImageBitmap(map)?.let {
            notification.setLargeIcon(it)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(channelId, "New Followers", manager)
        manager.notify("${map[NOTIFICATION_ID]}", 0, notification.build())

        Timber.d("Notification sent")
    }

    private fun sendLikeNotification(map: MutableMap<String, String>) {

        val channelId = "likes"
        val notification = getNotificationBuilder(channelId = channelId)
        notification.setContentText("${map["username"]}(${map["name"]}) liked your post.")

        getUserImageBitmap(map)?.let {
            notification.setLargeIcon(it)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(channelId, "Likes", manager)
        manager.notify("${map[NOTIFICATION_ID]}", 0, notification.build())

        Timber.d("Notification sent")
    }

    private fun removeNotificationIfPresent(map: MutableMap<String, String>) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel("${map[NOTIFICATION_ID]}", 0)
    }

    private fun getUserImageBitmap(map: MutableMap<String, String>): Bitmap? {
        try {
            map[USER_IMAGE_URL]?.let {
                val bitmap = BitmapFactory.decodeStream(URL(it).openConnection().getInputStream())
                return getCircleBitmap(bitmap)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getNotificationBuilder(
        pendingIntent: PendingIntent? = null,
        channelId: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.indigo_500))

    }

    private fun createChannel(
        channelId: String,
        channelName: String,
        manager: NotificationManager
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setShowBadge(true)
            channel.importance = NotificationManager.IMPORTANCE_HIGH
            manager.createNotificationChannel(channel)
        }
    }

    private fun getCircleBitmap(bitmap: Bitmap): Bitmap {
        val output: Bitmap
        val srcRect: Rect
        val dstRect: Rect
        val r: Float
        val width: Int = bitmap.width
        val height: Int = bitmap.height

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888)
            val left = (width - height) / 2
            val right = left + height
            srcRect = Rect(left, 0, right, height)
            dstRect = Rect(0, 0, height, height)
            r = height / 2.toFloat()
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
            val top = (height - width) / 2
            val bottom = top + width
            srcRect = Rect(0, top, width, bottom)
            dstRect = Rect(0, 0, width, width)
            r = width / 2.toFloat()
        }

        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(r, r, r, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        bitmap.recycle();
        return output
    }

}