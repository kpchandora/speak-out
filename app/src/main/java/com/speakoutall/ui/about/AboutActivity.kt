package com.speakoutall.ui.about

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.speakoutall.BuildConfig
import com.speakoutall.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        version.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        window?.statusBarColor = Color.BLACK
        window?.navigationBarColor = Color.BLACK
    }

}