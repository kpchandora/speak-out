package com.speakout.api

import com.google.gson.JsonObject
import com.speakout.posts.PostMiniDetails
import com.speakout.posts.create.PostData
import com.speakout.posts.tags.Tag
import retrofit2.Response
import retrofit2.http.*

/**
 * Created by Kalpesh on 29/07/20.
 */
public interface ApiService {

    @GET("tags")
    suspend fun getTags(@Query("tag") tag: String): Response<List<Tag>>

    @POST("tags/create")
    suspend fun createTag(@Body tag: Tag): Response<Tag>

    @POST("posts/create")
    suspend fun createPost(@Body post: PostData): Response<PostData>

    @GET("posts/getProfilePosts/{userId}")
    suspend fun getProfilePosts(
        @Header("userId") selfUserId: String,
        @Path("userId") userId: String
    ): Response<List<PostData>>

    @POST("posts/like")
    suspend fun likePost(@Body postMiniDetails: PostMiniDetails): Response<PostMiniDetails>

    @POST("posts/removeLike")
    suspend fun unLikePost(@Body postMiniDetails: PostMiniDetails): Response<PostMiniDetails>

    @DELETE("posts/delete/{postId}")
    suspend fun deletePost(
        @Header("userId") selfUserId: String,
        @Path("postId") postId: String
    ): Response<PostMiniDetails>
}