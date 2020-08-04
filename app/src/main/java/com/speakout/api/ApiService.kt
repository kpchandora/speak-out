package com.speakout.api

import com.google.gson.JsonObject
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
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

    @POST("users/create")
    suspend fun createUser(@Body userDetails: UserDetails): Response<UserDetails>

    @GET("users/get/{userId}")
    suspend fun getUser(
        @Header("userId") selfUserId: String,
        @Path("userId") userId: String
    ): Response<UserDetails>

    @GET("users/checkUsername/{username}")
    suspend fun checkUserName(@Path("username") username: String): Response<JsonObject>

    @POST("users/update")
    suspend fun updateUserDetails(@Body userMiniDetails: UserMiniDetails): Response<UserDetails>

    @POST("users/follow")
    suspend fun followUser(@Body jsonObject: JsonObject): Response<JsonObject>

    @POST("users/unfollow")
    suspend fun unFollowUser(@Body jsonObject: JsonObject): Response<JsonObject>

    @GET("users/followers/{userId}")
    suspend fun getFollowers(
        @Header("userId") selfUserId: String,
        @Path("userId") userId: String
    ): Response<List<UserMiniDetails>>

    @GET("users/followings/{userId}")
    suspend fun getFollowings(
        @Header("userId") selfUserId: String,
        @Path("userId") userId: String
    ): Response<List<UserMiniDetails>>

    @GET("users/likes/{postId}")
    suspend fun getLikes(
        @Header("userId") selfUserId: String,
        @Path("postId") postId: String
    ): Response<List<UserMiniDetails>>

    @GET("users/search")
    suspend fun searchUsers(@Query("username") username: String): Response<List<UserMiniDetails>>

}