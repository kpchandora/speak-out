package com.speakout.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtils {

    object FirestoreUtils {

        fun getRef() = FirebaseFirestore.getInstance()

        fun getUsersRef() =
            getRef().collection(NameUtils.DatabaseRefs.userDetailsRef)

        fun getTagsRef() = getRef().collection(NameUtils.DatabaseRefs.tags)

        fun getAllPostsRef() = getRef().collection(NameUtils.DatabaseRefs.postsRef)

        fun getSinglePostRef(postId: String) = getAllPostsRef().document(postId)

        fun getPostLikesRef(postId: String, userId: String) =
            getRef().document("post_likes/$postId/users/$userId")

        fun getFollowersFollowingsRef(userId: String) =
            getRef().document("followers_followings_count/${userId}")

        fun getFollowingsRef(userId: String) =
            getRef().document("/followings/${userId}")

        fun isFollowingRef(userId: String, selfId: String) =
            getRef().document("/followings/${selfId}/users/${userId}")


    }

    fun currentUser() = FirebaseAuth.getInstance().currentUser

    fun userId() = currentUser()?.uid

    fun signOut() {
        AppPreference.clearUserDetails()
        FirebaseAuth.getInstance().signOut()
    }

    fun getPostsStorageRef() = FirebaseStorage.getInstance().reference.child("posts")

    fun getProfilePictureStorageRef() = FirebaseStorage.getInstance().reference.child("profile")

    fun getReference() = FirebaseDatabase.getInstance().reference

    fun getTagsRef() = getReference().child("tags")

    enum class Data {
        PRESET,
        ABSENT,
        CANCELLED
    }

}