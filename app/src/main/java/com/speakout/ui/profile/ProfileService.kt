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
        FirebaseUtils.FirestoreUtils.getFollowingsUserRefs(AppPreference.getUserId())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        data.value = task.result?.let { snapShot ->
                            (snapShot.get("users") as? Map<String, DocumentReference>)?.containsKey(
                                userId
                            )
                        }
                    } catch (e: Exception) {
                        data.value = false
                    }
                } else {
                    data.value = null
                }
            }
        return data
    }

}