package com.speakout.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.speakout.custom.CustomProgressDialog
import com.speakout.utils.FirebaseUtils

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var mProgressDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProgressDialog = CustomProgressDialog(this)
    }

    fun showProgress() {
        mProgressDialog.show()
    }

    fun hideProgress() {
        mProgressDialog.dismiss()
    }

    fun currentUser() = FirebaseUtils.currentUser()

    fun userId() = FirebaseUtils.userId()

    fun signOut() = FirebaseUtils.signOut()


}