package com.speakout.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.speakout.auth.UserMiniDetails
import com.speakout.utils.AppPreference
import com.speakout.utils.FirebaseUtils
import io.reactivex.Single
import java.lang.Exception

object ProfileService {

    fun follow(userMiniDetails: UserMiniDetails): Single<Boolean> {
        return Single.create { emitter ->

            val db = FirebaseFirestore.getInstance()

            val currentFollowingsRef =
                db.document("followings/${AppPreference.getUserId()}/users/${userMiniDetails.userId}")

            val otherFollowersRef =
                db.document("followers/${userMiniDetails.userId}/users/${AppPreference.getUserId()}")

            db.runBatch {

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

                it.set(currentFollowingsRef, mapOf("timeStamp" to System.currentTimeMillis()))

                it.set(otherFollowersRef, mapOf("timeStamp" to System.currentTimeMillis()))

            }.addOnCompleteListener {
                if (it.isSuccessful) {
                    emitter.onSuccess(true)
                } else {
                    emitter.onError(it.exception!!)
                }
            }
        }
    }

    fun unFollowUser(userMiniDetails: UserMiniDetails): Single<Boolean> {
        return Single.create { emitter ->

            val db = FirebaseFirestore.getInstance()

            val currentFollowingsRef =
                db.document("followings/${AppPreference.getUserId()}/users/${userMiniDetails.userId}")

            val otherFollowersRef =
                db.document("followers/${userMiniDetails.userId}/users/${AppPreference.getUserId()}")

            db.runBatch {

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
                if (it.isSuccessful) {
                    emitter.onSuccess(true)
                } else {
                    emitter.onError(it.exception!!)
                }
            }
        }
    }

    fun isFollowing(userId: String): LiveData<Boolean?> {
        val data = MutableLiveData<Boolean?>()
        FirebaseUtils.FirestoreUtils.isFollowingRef(
            userId = userId,
            selfId = AppPreference.getUserId()
        ).get().addOnCompleteListener {
            if (it.isSuccessful) {
                data.value = it.result?.exists() ?: false
            } else {
                data.value = null
            }
        }

        return data
    }

}