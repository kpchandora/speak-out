package com.speakout

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.speakout.api.RetrofitBuilder
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class SpeakOutApp : Application() {

    companion object {
        @JvmStatic
        var appContext: Context? = null
            private set
    }
    private val userRepository by lazy {
        UsersRepository(RetrofitBuilder.apiService, AppPreference)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        FirebaseApp.initializeApp(applicationContext)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        if (AppPreference.isLoggedIn()){
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                GlobalScope.launch {
                    userRepository.updateFcmToken(it.token)
                }
            }
        }
    }

}