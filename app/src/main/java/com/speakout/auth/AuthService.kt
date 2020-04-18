package com.speakout.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.SetOptions
import com.speakout.common.Event
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.NameUtils
import java.lang.Exception

object AuthService {


    fun saveUserData(userDetails: UserDetails): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.userId()?.let {
            FirebaseUtils.FirestoreUtils.getUsersRef().document(it)
                .set(userDetails).addOnCompleteListener { task ->
                    data.value = task.isSuccessful
                }
        }
        return data
    }

    fun getUserData(uid: String): LiveData<UserDetails?> {
        val data = MutableLiveData<UserDetails?>()
        FirebaseUtils.FirestoreUtils.getUsersRef().document(uid)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    try {
                        data.value = it.result?.toObject(UserDetails::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        data.value = null
                    }
                } else {
                    data.value = null
                }
            }
        return data
    }

    fun updateUserData(map: Map<String, Any>): LiveData<Event<Boolean>> {
        val data = MutableLiveData<Event<Boolean>>()
        FirebaseUtils.userId()?.let {
            FirebaseUtils.FirestoreUtils.getUsersRef().document(it)
                .update(map).addOnCompleteListener { task ->
                    data.value = Event(task.isSuccessful)
                }
        }
        return data
    }


    fun isUsernamePresent(key: String): LiveData<FirebaseUtils.Data> {
        val data = MutableLiveData<FirebaseUtils.Data>()
        FirebaseUtils.FirestoreUtils.getUsersRef()
            .whereEqualTo("username", key)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    try {

                        data.value =
                            if (it.result?.toObjects(UserDetails::class.java)?.isNotEmpty() == true) FirebaseUtils.Data.PRESET
                            else FirebaseUtils.Data.ABSENT


                    } catch (e: Exception) {
                        e.printStackTrace()
                        data.value = FirebaseUtils.Data.ABSENT
                    }
                } else {
                    data.value = FirebaseUtils.Data.CANCELLED
                }
            }
        return data
    }

}