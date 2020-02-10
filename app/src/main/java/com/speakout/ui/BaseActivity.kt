package com.speakout.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.speakout.utils.FirebaseUtils

abstract class BaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun currentUser() = FirebaseUtils.currentUser()

    fun userId() = FirebaseUtils.userId()

    fun signOut() = FirebaseUtils.signOut()


}