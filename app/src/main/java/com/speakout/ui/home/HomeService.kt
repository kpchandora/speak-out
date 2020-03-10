package com.speakout.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.speakout.posts.create.PostData
import com.speakout.utils.FirebaseUtils

object HomeService {

    fun getPosts(): LiveData<List<PostData>> {
        val data = MutableLiveData<List<PostData>>()
        FirebaseUtils.FirestoreUtils.getPostsRef().get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    data.value = emptyList()
                } else {
                    val list = mutableListOf<PostData>()
                    it.forEach { document ->
                        try {
                            list.add(document.toObject(PostData::class.java))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    data.value = list
                }
            }.addOnFailureListener {
                data.value = emptyList()
            }

        return data
    }

}