package com.speakout.posts.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getTagsRef
import com.speakout.utils.StringJava
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception

object TagsService {

    fun addTag(tag: Tag): LiveData<Tag?> {
        val data = MutableLiveData<Tag?>()
        FirebaseUtils.FirestoreUtils.getTagsRef().document(tag.id.toString()).set(tag)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    data.value = tag
                }
            }
        return data
    }


    suspend fun getTags(query: String): List<Tag> = withContext(Dispatchers.IO) {
        try {
            val nextChar = StringJava.next(query)
            Timber.d("Next String: $nextChar")
            var ref = getTagsRef()
                .whereGreaterThanOrEqualTo("tag", query)

            if (query.isNotEmpty()) {
                ref = ref.whereLessThan("tag", nextChar)
            }
            val querySnapshot = ref.limit(20).get().await()

            val list = mutableListOf<Tag>()
            querySnapshot.forEach { document ->
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

            if (query.trim().isNotEmpty()) {
                val count = list.count { tag ->
                    query == tag.tag
                }
                if (count == 0) {
                    val tag = Tag(tag = query, id = System.nanoTime(), used = null)
                    list.add(0, tag)
                }
            }
            list
        } catch (e: Exception) {
            Timber.e(e)
            emptyList<Tag>()
        }
    }

}