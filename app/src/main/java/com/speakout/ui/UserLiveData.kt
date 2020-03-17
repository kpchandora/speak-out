package com.speakout.ui

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.speakout.auth.UserDetails
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import com.speakout.utils.FirebaseUtils
import timber.log.Timber
import java.lang.Exception

class UserLiveData : LiveData<UserDetails?>() {

    private var listener: ListenerRegistration? = null
    override fun onActive() {
        super.onActive()
        listener = FirebaseUtils.FirestoreUtils.getUsersRef().document(AppPreference.getUserId())
            .addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    try {
                        postValue(documentSnapshot.toObject(UserDetails::class.java))
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