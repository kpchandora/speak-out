package com.speakout.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtils {

    fun currentUser() = FirebaseAuth.getInstance().currentUser

    fun userId() = currentUser()?.uid

    fun signOut(activity: Activity) {
        AppPreference.clearUserDetails()
        FirebaseAuth.getInstance().signOut()
        val client = GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )
        client.signOut()
        client.revokeAccess()
    }

    fun getPostsStorageRef() = FirebaseStorage.getInstance().reference.child("posts")

    fun getProfilePictureStorageRef() = FirebaseStorage.getInstance().reference.child("profile")

}