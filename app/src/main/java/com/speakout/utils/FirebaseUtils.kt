package com.speakout.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.speakout.SpeakOutApp

object FirebaseUtils {

    fun currentUser() = FirebaseAuth.getInstance().currentUser

    fun userId() = currentUser()?.uid

    fun signOut() {
        Preference().clearUserDetails()
        FirebaseAuth.getInstance().signOut()
    }

    fun getReference() = FirebaseDatabase.getInstance().reference

    enum class Data {
        PRESET,
        ABSENT,
        CANCELLED
    }

}