package com.speakout.utils

import com.google.firebase.auth.FirebaseAuth
import com.speakout.utils.NameUtils.DatabaseRefs.userDetailsRef as userDetails
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtils {

    fun getFirebaseFunction(functionName: String) =
        FirebaseFunctions.getInstance().getHttpsCallable(functionName)

    object FirestoreUtils {

        fun getRef() = FirebaseFirestore.getInstance()

        fun getUsersRef() =
            getRef().collection(NameUtils.DatabaseRefs.userDetailsRef)

        fun getSingleUserRef(userId: String) = getRef().document("user_details/$userId")

        fun getUsersPostRef(userId: String, postId: String) =
            getRef().document("user_details/$userId/posts/$postId")

        fun getTagsRef() = getRef().collection(NameUtils.DatabaseRefs.tags)

        fun getAllPostsRef() = getRef().collection(NameUtils.DatabaseRefs.postsRef)

        fun getSinglePostRef(postId: String) = getAllPostsRef().document(postId)

        fun getPostSingleLikeRef(postId: String) =
            getRef().document("post_likes/$postId/")

        fun getPostLikesRef(postId: String) = getRef().document("post_likes/$postId")

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