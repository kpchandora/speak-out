package com.speakout.ui

import android.os.Bundle
import android.view.View
import com.speakout.R
import com.speakout.utils.FirebaseUtils

class MainActivity1 : BaseActivity() {

    companion object {
        private const val RC_SIGN_IN = 101
        const val SIGN_IN_DATA = "sign_in_data"
        const val SIGN_IN_TYPE = "sign_n_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)
    }

    fun signOut(view: View) {
        FirebaseUtils.signOut()
        finish()
    }

}
