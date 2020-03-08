package com.speakout.posts.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.StringJava
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
        val nextChar = StringJava.next(query)
        Timber.d("Next String: $nextChar")
        var ref = FirebaseUtils.FirestoreUtils.getTagsRef()
            .whereGreaterThanOrEqualTo("tag", query)

        if (query.isNotEmpty()) {
            ref = ref.whereLessThan("tag", nextChar)
        }
        ref.limit(20)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    data.value = emptyList()
                } else {
                    val list = mutableListOf<Tag>()
                    it.forEach { document ->
                        try {
                            list.add(document.toObject(Tag::class.java))
                        } catch (e: Exception) {
                            Timber.e("Error: $e")
                        }
                    }
                    list.sortByDescending { tag ->
                        tag.used
                    }
                    Timber.d("Sorted: $list")
                    data.value = list
                }
            }.addOnFailureListener {
                data.value = emptyList()
            }
        return data
    }

}