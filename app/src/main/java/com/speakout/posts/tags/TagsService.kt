package com.speakout.posts.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.speakout.utils.FirebaseUtils
import timber.log.Timber
import java.lang.Exception

object TagsService {

    fun checkTagPresent(tag: Tag): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.FirestoreUtils.getTagsRef().document(tag.id.toString()).set(tag)
        return data
    }

    fun getTags(query: String): LiveData<List<Tag>> {
        val data = MutableLiveData<List<Tag>>()
        FirebaseUtils.getTagsRef().orderByChild("tag").startAt(query)
            .limitToFirst(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    data.value = emptyList()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        try {
                            val list = mutableListOf<Tag>()
                            p0.children.forEach {
                                it.getValue(Tag::class.java)?.let { tag ->
                                    list.add(tag)
                                }
//                                Timber.d("Value: ${it.value}")
                            }
                            data.value = list
                        } catch (e: Exception) {
                            e.printStackTrace()
                            data.value = emptyList()
                        }
                    } else {
                        data.value = emptyList()
                    }
                }

            })
        return data
    }

    fun getTagsFirestore(query: String): LiveData<List<Tag>> {
        val data = MutableLiveData<List<Tag>>()
        FirebaseUtils.FirestoreUtils.getTagsRef().get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    data.value = emptyList()
                } else {
                    it.forEach {
                        try {
                            Timber.d("Tag: ${it.toObject(Tag::class.java)}")
                        } catch (e: Exception) {
                            Timber.e("Error: $e")
                        }
                    }
                }
            }.addOnFailureListener {
                data.value = emptyList()
            }
        return data
    }

}