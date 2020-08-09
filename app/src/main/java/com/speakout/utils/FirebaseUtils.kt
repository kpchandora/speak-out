package com.speakout.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtils {

    fun currentUser() = FirebaseAuth.getInstance().currentUser

    fun userId() = currentUser()?.uid

    fun signOut() {
        AppPreference.clearUserDetails()
        FirebaseAuth.getInstance().signOut()
    }

    fun getPostsStorageRef() = FirebaseStorage.getInstance().reference.child("posts")

    fun getProfilePictureStorageRef() = FirebaseStorage.getInstance().reference.child("profile")

}