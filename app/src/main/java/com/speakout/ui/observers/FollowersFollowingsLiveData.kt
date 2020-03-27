package com.speakout.ui.observers

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.ListenerRegistration
import com.speakout.ui.profile.FollowersFollowingsData
import com.speakout.utils.FirebaseUtils
import java.lang.Exception

class FollowersFollowingsLiveData(private val userId: String) :
    LiveData<FollowersFollowingsData?>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()
        listener = FirebaseUtils.FirestoreUtils.getFollowersFollowingsRef(userId)
            .addSnapshotListener { documentSnapshot, _ ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    try {
                        postValue(documentSnapshot.toObject(FollowersFollowingsData::class.java))
                    } catch (e: Exception) {
                        postValue(null)
                    }
                } else {
                    postValue(null)
                }
            }
    }

    override fun onInactive() {
        listener?.remove()
        super.onInactive()
    }

}