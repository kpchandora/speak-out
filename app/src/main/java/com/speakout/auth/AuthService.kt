package com.speakout.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.SetOptions
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.NameUtils
import java.lang.Exception

object AuthService {

    fun saveUserData(
        userDetails: UserDetails,
        ref: String = NameUtils.DatabaseRefs.userDetailsRef
    ): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.userId()?.let {
            FirebaseUtils.getReference().child(ref)
                .child(it).setValue(userDetails).addOnCompleteListener { task ->
                    data.value = task.isSuccessful
                }
        }
        return data
    }

    fun saveUserDataFirestore(userDetails: UserDetails): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.userId()?.let {
            FirebaseUtils.FirestoreUtils.getUsersRef().document(it)
                .set(userDetails).addOnCompleteListener { task ->
                    data.value = task.isSuccessful
                }
        }
        return data
    }

    fun getUserDataFirestore(uid: String): LiveData<UserDetails?> {
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

    fun getUserData(uid: String): LiveData<UserDetails?> {
        val data = MutableLiveData<UserDetails?>()
        FirebaseUtils.getReference().child(NameUtils.DatabaseRefs.userDetailsRef).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    data.value = null
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        try {
                            data.value = p0.getValue(UserDetails::class.java)
                        } catch (e: Exception) {
                            data.value = null
                        }
                    } else {
                        data.value = null
                    }
                }
            })
        return data
    }

    fun updateUserDataFirestore(map: Map<String, Any>): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.userId()?.let {
            FirebaseUtils.FirestoreUtils.getUsersRef().document(it)
                .update(map).addOnCompleteListener { task ->
                    data.value = task.isSuccessful
                }
        }
        return data
    }

    fun updateUserData(map: Map<String, Any>): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.userId()?.let {
            FirebaseUtils.getReference().child(NameUtils.DatabaseRefs.userDetailsRef).child(it)
                .updateChildren(map)
                .addOnCompleteListener { task ->
                    data.value = task.isSuccessful
                }
        }

        return data
    }

    fun isUsernamePresentFirestore(key: String): LiveData<FirebaseUtils.Data> {
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

    fun isUsernamePresent(key: String): LiveData<FirebaseUtils.Data> {
        val data = MutableLiveData<FirebaseUtils.Data>()
        FirebaseUtils.getReference().child(NameUtils.DatabaseRefs.userDetailsRef)
            .orderByChild("username")
            .equalTo(key)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        data.value = FirebaseUtils.Data.CANCELLED
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        data.value = if (p0.exists())
                            FirebaseUtils.Data.PRESET
                        else
                            FirebaseUtils.Data.ABSENT
                    }
                })

        return data
    }
}