package com.speakout.api

import com.speakout.posts.tags.Tag
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Kalpesh on 29/07/20.
 */
public interface ApiService {

    @GET("tags")
    suspend fun getTags(@Query("tag") tag: String): Response<List<Tag>>

    @POST("tags/create")
    suspend fun createTag(@Body tag: Tag): Response<Tag>
}