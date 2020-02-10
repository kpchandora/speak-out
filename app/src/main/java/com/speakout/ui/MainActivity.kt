package com.speakout.ui

import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.speakout.R

class MainActivity : BaseActivity() {

    companion object {
        private const val RC_SIGN_IN = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initiateAuthTypes()
    }

    private fun initiateAuthTypes() {
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
    }
}
