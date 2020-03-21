package com.speakout.auth

import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.speakout.R
import com.speakout.extensions.*
import com.speakout.ui.BaseActivity
import com.speakout.ui.MainActivity
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.activity_user_name.*

class UserNameActivity : BaseActivity() {

    private lateinit var mUserViewModel: UserViewModel
    private var username = ""
    private val userNameRegex = "^[a-z0-9_]{1,25}\$".toRegex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_name)
        mUserViewModel = ViewModelProvider.NewInstanceFactory().create(UserViewModel::class.java)

        user_name_next_btn.isEnabled = false

        mUserViewModel.usernameObserver.observe(this, Observer {
            it?.apply {
                when (this) {
                    FirebaseUtils.Data.PRESET -> {
                        profile_edit_username_til.error = "Username is already taken"
                    }
                    FirebaseUtils.Data.ABSENT -> {
                        profile_edit_username_et.setDrawableEnd(R.drawable.ic_check)
                        user_name_next_btn.isEnabled = true
                        profile_edit_username_til.error = null
                    }
                    FirebaseUtils.Data.CANCELLED -> {
                        showShortToast(getString(R.string.error_something_went_wrong))
                    }
                }
            }
        })

        mUserViewModel.updateDetailsObserver.observe(this, Observer {
            hideProgress()
            if (it) {
                AppPreference.saveUserDetails(UserDetails(username = username))
                openActivity(MainActivity::class.java)
                finish()
            } else {
                showShortToast(getString(R.string.error_something_went_wrong))
            }
        })

        profile_edit_username_et.doOnTextChanged { text: CharSequence?, start: Int,
                                                   count: Int, after: Int ->

            profile_edit_username_et.removeDrawableEnd()
//            user_unique_name_et.setText(text?.toString()?.toLowerCase(Locale.getDefault()) ?: "")

            text?.let {
                user_name_next_btn.isEnabled = false
                if (userNameRegex.matches(text)) {
                    if (text.length < 3) {
                        profile_edit_username_til.error = "Username is too small"
                    } else {
                        profile_edit_username_til.error = null
                        username = text.toString()
                        mUserViewModel.isUsernamePresent(username)
                    }
                } else {
                    profile_edit_username_til.error = "Please enter a valid username"
                }
            }

        }

        user_name_next_btn.setOnClickListener {
            showProgress()
            mUserViewModel.updateUserDetails(mapOf(UserDetails.updateUsername(username)))
        }

    }

    override fun onDestroy() {
        FirebaseUtils.signOut()
        super.onDestroy()
    }

}
