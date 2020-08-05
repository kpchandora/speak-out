package com.speakout.utils

import com.google.firebase.auth.FirebaseAuth
import com.speakout.utils.NameUtils.DatabaseRefs.userDetailsRef as userDetails
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
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