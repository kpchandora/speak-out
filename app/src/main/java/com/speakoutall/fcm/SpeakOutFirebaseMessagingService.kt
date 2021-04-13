package com.speakoutall.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.extensions.isNotNullOrEmpty
import com.speakoutall.ui.MainActivity
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.net.URL


/**
 * Created by Kalpesh on 09/08/20.
 */
class SpeakOutFirebaseMessagingService : FirebaseMessagingService() {

    private val appPreference = AppPreference
    private val userRepository by lazy {
        UsersRepository(RetrofitBuilder.apiService, appPreference)
    }

    companion object {
        const val FOLLOW = "follow"
        const val UNFOLLOW = "unfollow"
        const val LIKE = "like"
        const val NOTIFICATION_ID = "notificationId"
        const val USER_ID = "userId"
        const val OTHER = "other"
        const val USER_IMAGE_URL = "profileUrl"
        const val POST_ID = "postId"
        const val USERNAME = "username"
        const val POST_IMAGE_URL = "postImageUrl"
    }

    override fun onNewToken(token: String) {
        Timber.d("Token: $token")
        if (appPreference.isLoggedIn()) {
            GlobalScope.launch {
                userRepository.updateFcmToken(token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("Data: ${remoteMessage.data}")
        if (!appPreference.isLoggedIn()) return
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
        val channelId = "new_followers"

        val bundle = Bundle().apply {
            putString(USER_ID, map[USER_ID])
            putString(USERNAME, map[USERNAME])
            putString(USER_IMAGE_URL, map[USER_IMAGE_URL])
        }
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.navigation_profile)
            .setArguments(bundle)
            .createPendingIntent()

        val notification =
            getNotificationBuilder(channelId = channelId, pendingIntent = pendingIntent)
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
        val bundle = Bundle().apply {
            putString(POST_ID, map[POST_ID])
            putBoolean(applicationContext.getString(R.string.is_from_deep_link), true)
        }
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.notificationFragment)
            .setArguments(bundle)
            .createPendingIntent()
        val notification =
            getNotificationBuilder(channelId = channelId, pendingIntent = pendingIntent)
        notification.setContentText("${map["username"]}(${map["name"]}) liked your post.")

        getUserImageBitmap(map)?.let { userBitmap ->
            notification.setLargeIcon(userBitmap)
            getPostImageBitmap(map[POST_IMAGE_URL])?.let {
                notification.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(it)
                        .bigLargeIcon(userBitmap)
                )
            }
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

    private fun getPostImageBitmap(postImageUrl: String?): Bitmap? {
        try {
            return BitmapFactory.decodeStream(URL(postImageUrl).openConnection().getInputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return null
    }

    private fun getUserImageBitmap(
        map: MutableMap<String, String>
    ): Bitmap? {
        try {
            map[USER_IMAGE_URL]?.let {
                val bitmap = BitmapFactory.decodeStream(URL(it).openConnection().getInputStream())
                return getCircleBitmap(bitmap)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
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
            .setContentIntent(pendingIntent)
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

    /**
     * This method converts the given bitmap to circular bitmap
     */
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