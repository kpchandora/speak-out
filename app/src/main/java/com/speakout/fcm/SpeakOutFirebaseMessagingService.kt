package com.speakout.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.speakout.api.RetrofitBuilder
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import kotlinx.coroutines.CoroutineScope
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("Data: ${remoteMessage.data}")
        Timber.d("Notification: ${remoteMessage.notification}")
    }

    override fun onNewToken(token: String) {
        Timber.d("Token: $token")
        GlobalScope.launch {
            userRepository.updateFcmToken(token)
        }
    }

}