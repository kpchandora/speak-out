package com.speakout.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.speakout.R
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.loadImage
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.activity_profile_edit.*

class ProfileEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        val screenSize = getScreenSize()
        profile_edit_update_btn.layoutParams.width = screenSize.widthPixels / 4
        profile_edit_iv.layoutParams.width = screenSize.widthPixels / 3
        profile_edit_iv.loadImage(
            AppPreference.getPhotoUrl(),
            R.drawable.ic_profile_placeholder,
            true
        )
        profile_edit_username_et
    }
}
