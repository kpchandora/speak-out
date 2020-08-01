package com.speakout.posts.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import com.speakout.api.RetrofitBuilder
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
        getTagsRef().document(tag.id.toString()).set(tag)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    data.value = tag
                }
            }
        return data
    }

    suspend fun addTagToDb(tag: Tag): Tag? = withContext(Dispatchers.IO) {
        try {
            val result = RetrofitBuilder.apiService.createTag(tag)
            if (result.isSuccessful && result.body() != null) {
                return@withContext result.body()!!
            }
            null
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun getTagsFromRemote(query: String): List<Tag> = withContext(Dispatchers.IO) {
        try {
            val result = RetrofitBuilder.apiService.getTags(query)
            val list = mutableListOf<Tag>()
            if (result.isSuccessful && result.body() != null) {
                list.addAll(result.body()!!)
                if (query.trim().isNotEmpty()) {
                    val count = list.count { tag ->
                        query == tag.tag
                    }
                    if (count == 0) {
                        val tag = Tag(tag = query, id = System.nanoTime(), used = null)
                        list.add(0, tag)
                    }
                }
            }
            list
        } catch (e: Exception) {
            Timber.e(e)
            emptyList<Tag>()
        }
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