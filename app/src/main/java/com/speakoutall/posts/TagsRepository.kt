package com.speakoutall.posts

import com.speakoutall.api.ApiService
import com.speakoutall.posts.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception

/**
 * Created by Kalpesh on 29/07/20.
 */
public class TagsRepository(private val apiService: ApiService) {

    suspend fun getTags(query: String): List<Tag> = withContext(Dispatchers.IO) {
        try {
            val result = apiService.getTags(query)
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

    suspend fun createTag(tag: Tag): Tag? = withContext(Dispatchers.IO) {
        try {
            val result = apiService.createTag(tag)
            return@withContext result.body()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

}