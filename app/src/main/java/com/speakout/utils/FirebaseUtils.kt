package com.speakout.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.speakout.SpeakOutApp

object FirebaseUtils {

    object FirestoreUtils {

        fun getRef() = FirebaseFirestore.getInstance()

        fun getUsersRef() =
            getRef().collection(StringUtils.DatabaseRefs.userDetailsRef)

        fun getTagsRef() = getRef().collection(StringUtils.DatabaseRefs.tags)

    }

    fun currentUser() = FirebaseAuth.getInstance().currentUser

    fun userId() = currentUser()?.uid

    fun signOut() {
        Preference().clearUserDetails()
        FirebaseAuth.getInstance().signOut()
    }

    fun getPostsStorageRef() = FirebaseStorage.getInstance().reference.child("posts")

    fun getProfilePictureStorageRef() = FirebaseStorage.getInstance().reference.child("profile_pic")

    fun getReference() = FirebaseDatabase.getInstance().reference

    fun getTagsRef() = getReference().child("tags")

    enum class Data {
        PRESET,
        ABSENT,
        CANCELLED
    }

}