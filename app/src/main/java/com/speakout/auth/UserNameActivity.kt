package com.speakout.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.speakout.R
import com.speakout.utils.FirebaseUtils
import kotlinx.android.synthetic.main.activity_user_name.*

class UserNameActivity : AppCompatActivity() {

    private lateinit var mUserViewModel: UserViewModel
    private var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_name)
        mUserViewModel = ViewModelProvider.NewInstanceFactory().create(UserViewModel::class.java)

        mUserViewModel.usernameObserver.observe(this, Observer {
            it?.apply {
                when (this) {
                    FirebaseUtils.Data.PRESET -> {
                        Toast.makeText(
                            this@UserNameActivity,
                            "Username already taken",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    FirebaseUtils.Data.ABSENT -> {
                        Toast.makeText(this@UserNameActivity, "Absent", Toast.LENGTH_SHORT).show()
                        mUserViewModel.updateUserDetails(UserDetails.usernameMap(username))
                    }
                    FirebaseUtils.Data.CANCELLED -> {
                        Toast.makeText(this@UserNameActivity, "Cancelled", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })

        mUserViewModel.saveUserDetailsObserver.observe(this, Observer {
            if (it) {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        })

        mUserViewModel.updateDetailsObserver.observe(this, Observer {
            if (it) {
                Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })

        user_name_done_btn.setOnClickListener {
            user_unique_name_et.text.toString().trim().let {
                if (it.isNotEmpty()) {
                    username = it
                    mUserViewModel.isUsernamePresent(it)
                } else {
                    user_unique_name_et.error = "Field can't be empty"
                }
            }

        }
    }

    override fun onDestroy() {
        FirebaseUtils.signOut()
        super.onDestroy()
    }

}
