package com.speakout.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.speakout.utils.FirebaseUtils

object HomeService {

    fun listenDemo(): LiveData<String> {
        val data = MutableLiveData<String>()
        FirebaseUtils.getReference().child("demo").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                data.value = p0.message
            }

            override fun onDataChange(p0: DataSnapshot) {
                (p0.value as? String)?.let {
                    data.value = it
                } ?: kotlin.run { data.value = "Error" }
            }
        })
        return data
    }

}