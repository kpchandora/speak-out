package com.speakout.people

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.speakout.auth.UserMiniDetails
import com.speakout.utils.AppPreference
import com.speakout.utils.NameUtils

object FollowUnfollowService {

    fun follow(userMiniDetails: UserMiniDetails): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        val db = FirebaseFirestore.getInstance()

        val currentFollowingsRef =
            db.document("followings/${AppPreference.getUserId()}/users/${userMiniDetails.userId}")

        val otherFollowersRef =
            db.document("followers/${userMiniDetails.userId}/users/${AppPreference.getUserId()}")

        val followingsUsersRefs =
            db.document("followingsUsersRefs/${AppPreference.getUserId()}")
        val followingsUsersRefsData = mapOf(
            "users" to mapOf(userMiniDetails.userId to otherFollowersRef)
        )

        val followersUsersRefs =
            db.document("followersUsersRefs/${userMiniDetails.userId}")
        val followersUsersRefsData = mapOf(
            "users" to mapOf(AppPreference.getUserId() to currentFollowingsRef)
        )

        db.runBatch {

            it.set(followersUsersRefs, followersUsersRefsData, SetOptions.merge())
            it.set(followingsUsersRefs, followingsUsersRefsData, SetOptions.merge())

            val followersFollowingsCountSelfRef =
                db.document("followers_followings_count/${AppPreference.getUserId()}")

            val followersFollowingsCountOtherRef =
                db.document("followers_followings_count/${userMiniDetails.userId}")

            it.set(
                followersFollowingsCountSelfRef,
                mapOf("followingsCount" to FieldValue.increment(1)),
                SetOptions.merge()
            )

            it.set(
                followersFollowingsCountOtherRef,
                mapOf("followersCount" to FieldValue.increment(1)),
                SetOptions.merge()
            )

            it.set(currentFollowingsRef, userMiniDetails)

            it.set(
                otherFollowersRef, UserMiniDetails(
                    name = AppPreference.getUserDisplayName(),
                    userId = AppPreference.getUserId(),
                    photoUrl = AppPreference.getPhotoUrl(),
                    username = AppPreference.getUserUniqueName()
                )
            )

        }.addOnCompleteListener {
            data.value = it.isSuccessful
        }
        return data
    }

    fun unfollowUser(userMiniDetails: UserMiniDetails): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        val db = FirebaseFirestore.getInstance()

        val currentFollowingsRef =
            db.document("followings/${AppPreference.getUserId()}/users/${userMiniDetails.userId}")

        val otherFollowersRef =
            db.document("followers/${userMiniDetails.userId}/users/${AppPreference.getUserId()}")

        val followingsUsersRefs =
            db.document("followingsUsersRefs/${AppPreference.getUserId()}")
        val followingsUsersRefsData = mapOf(
            "users" to mapOf(userMiniDetails.userId to FieldValue.delete())
        )

        val followersUsersRefs =
            db.document("followersUsersRefs/${userMiniDetails.userId}")
        val followersUsersRefsData = mapOf(
            "users" to mapOf(AppPreference.getUserId() to FieldValue.delete())
        )

        db.runBatch {

            it.set(followersUsersRefs, followersUsersRefsData, SetOptions.merge())
            it.set(followingsUsersRefs, followingsUsersRefsData, SetOptions.merge())

            val followersFollowingsCountSelfRef =
                db.document("followers_followings_count/${AppPreference.getUserId()}")

            val followersFollowingsCountOtherRef =
                db.document("followers_followings_count/${userMiniDetails.userId}")

            it.set(
                followersFollowingsCountSelfRef,
                mapOf("followingsCount" to FieldValue.increment(-1)),
                SetOptions.merge()
            )

            it.set(
                followersFollowingsCountOtherRef,
                mapOf("followersCount" to FieldValue.increment(-1)),
                SetOptions.merge()
            )


            it.delete(currentFollowingsRef)
            it.delete(otherFollowersRef)

        }.addOnCompleteListener {
            data.value = it.isSuccessful
        }
        return data
    }

    fun unfollow(userMiniDetails: UserMiniDetails): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        val db = FirebaseFirestore.getInstance()

        // Reference of current user who is following other user
        val currentUserRef =
            db.collection(NameUtils.DatabaseRefs.userDetailsRef).document(AppPreference.getUserId())

        // Reference of user who is getting followed
        val otherUserRef =
            db.collection(NameUtils.DatabaseRefs.userDetailsRef).document(userMiniDetails.userId)

        val currentFollowingRef =
            db.document("followings/${AppPreference.getUserId()}/users/${userMiniDetails.userId}")

        val otherFollowersRef =
            db.document("followers/${userMiniDetails.userId}/users/${AppPreference.getUserId()}")

        db.runBatch {
            it.update(currentUserRef, mapOf("followingsCount" to FieldValue.increment(-1)))
            it.update(otherUserRef, mapOf("followersCount" to FieldValue.increment(-1)))

            currentFollowingRef.delete()
            otherFollowersRef.delete()

        }.addOnCompleteListener {
            data.value = it.isSuccessful
        }
        return data
    }

}