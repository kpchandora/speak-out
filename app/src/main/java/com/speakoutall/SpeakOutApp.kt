package com.speakoutall

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import timber.log.Timber

class SpeakOutApp : Application() {

    companion object {
        @JvmStatic
        var appContext: Context? = null
            private set
    }


    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        FirebaseApp.initializeApp(applicationContext)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}