package com.speakout.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.StringUtils
import java.lang.Exception

object AuthService {

    fun saveUserData(
        userDetails: UserDetails,
        ref: String = StringUtils.DatabaseRefs.userDetailsRef
    ): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.userId()?.let {
            FirebaseUtils.getReference().child(ref)
                .child(it).setValue(userDetails).addOnCompleteListener { task ->
                    data.value = task.isSuccessful
                }.addOnFailureListener {
                    data.value = false
                }
        }

        return data
    }

    fun getUserData(uid: String): LiveData<UserDetails?> {
        val data = MutableLiveData<UserDetails?>()
        FirebaseUtils.getReference().child(StringUtils.DatabaseRefs.userDetailsRef).child(uid)
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

    fun updateUserData(
        map: Map<String, Any>
    ): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.userId()?.let {
            FirebaseUtils.getReference().child(StringUtils.DatabaseRefs.userDetailsRef).child(it)
                .updateChildren(map)
                .addOnCompleteListener { task ->
                    data.value = task.isSuccessful
                }.addOnFailureListener {
                    data.value = false
                }
        }

        return data
    }

    fun isUsernamePresent(key: String): LiveData<FirebaseUtils.Data> {
        val data = MutableLiveData<FirebaseUtils.Data>()
        FirebaseUtils.getReference().child(StringUtils.DatabaseRefs.userDetailsRef)
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