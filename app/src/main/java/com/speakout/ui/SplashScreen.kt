package com.speakout.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import com.speakout.R

class SplashScreen : AppCompatActivity() {

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler()
        handler?.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000)

    }


    override fun onBackPressed() {
        return
    }

    override fun onDestroy() {
        handler?.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
