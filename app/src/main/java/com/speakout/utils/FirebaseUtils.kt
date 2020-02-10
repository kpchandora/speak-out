package com.speakout.utils

import com.google.firebase.auth.FirebaseAuth

object FirebaseUtils {

    fun currentUser() = FirebaseAuth.getInstance().currentUser

    fun userId() = currentUser()?.uid

    fun signOut() = FirebaseAuth.getInstance().signOut()

}