package com.speakout.posts.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.speakout.utils.FirebaseUtils
import timber.log.Timber
import java.lang.Exception

object TagsService {

    fun checkTagPresent(tag: Tag): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        return data
    }

    fun getTags(): LiveData<List<Tag>> {
        val data = MutableLiveData<List<Tag>>()
        FirebaseUtils.getTagsRef().addListenerForSingleValueEvent(object : ValueEventListener {
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
                            Timber.d("Value: ${it.value}")
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

}