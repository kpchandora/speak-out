package com.speakout.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.speakout.auth.SignInActivity

class SplashScreen : AppCompatActivity() {

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        handler = Handler()
        handler?.postDelayed({
            startActivity(Intent(this, SignInActivity::class.java))
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
