package com.speakout.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.firebase.ui.auth.AuthUI
import com.speakout.R

class SplashScreen : AppCompatActivity() {

    private var handler: Handler? = null

    companion object {
        private const val RC_SIGN_IN = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler()
        handler?.postDelayed({
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                        listOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.PhoneBuilder()
                                .setDefaultCountryIso("in")
                                .build()
                        )
                    ).build(),
                RC_SIGN_IN
            )
            finish()
        }, 2000)
    }

    override fun onBackPressed() {
        return
    }

    override fun onDestroy() {
        handler?.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
