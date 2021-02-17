package com.speakout.api

import com.google.gson.JsonObject
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.notification.NotificationResponse
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
        @Path("userId") userId: String,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<PostData>>

    @GET("posts/getFeed")
    suspend fun getFeed(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<PostData>>

    @POST("posts/like")
    suspend fun likePost(@Body postMiniDetails: PostMiniDetails): Response<PostMiniDetails>

    @POST("posts/removeLike")
    suspend fun unLikePost(@Body postMiniDetails: PostMiniDetails): Response<PostMiniDetails>

    @DELETE("posts/delete/{postId}")
    suspend fun deletePost(@Path("postId") postId: String): Response<PostMiniDetails>

    @POST("users/create")
    suspend fun createUser(@Body userDetails: UserDetails): Response<UserDetails>

    @GET("users/get/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserDetails>

    @GET("users/checkUsername/{username}")
    suspend fun checkUserName(@Path("username") username: String): Response<JsonObject>

    @POST("users/update")
    suspend fun updateUserDetails(@Body userMiniDetails: UserMiniDetails): Response<UserDetails>

    @POST("users/follow")
    suspend fun followUser(@Body jsonObject: JsonObject): Response<UserDetails>

    @POST("users/unfollow")
    suspend fun unFollowUser(@Body jsonObject: JsonObject): Response<UserDetails>

    @GET("users/followers/{userId}")
    suspend fun getFollowers(
        @Path("userId") userId: String, @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<UserMiniDetails>>

    @GET("users/followings/{userId}")
    suspend fun getFollowings(
        @Path("userId") userId: String, @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<UserMiniDetails>>

    @GET("users/likes/{postId}")
    suspend fun getLikes(
        @Path("postId") postId: String, @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<UserMiniDetails>>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("username") username: String, @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<UserMiniDetails>>

    @POST("users/updateToken")
    suspend fun updateFcmToken(@Body fcmToken: JsonObject): Response<JsonObject>

    @GET("posts/getSinglePost/{postId}")
    suspend fun getSinglePost(@Path("postId") postId: String): Response<PostData>

    @GET("notifications/all")
    suspend fun getNotifications(): Response<List<NotificationResponse>>

    @POST("posts/addBookmark")
    suspend fun addBookmark(@Body jsonObject: JsonObject): Response<JsonObject>

    @POST("posts/removeBookmark")
    suspend fun removeBookmark(@Body jsonObject: JsonObject): Response<JsonObject>
}