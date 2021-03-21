package com.speakout.posts

import android.graphics.Bitmap
import com.google.gson.JsonObject
import com.speakout.api.ApiService
import com.speakout.api.BaseRepository
import com.speakout.common.Result
import com.speakout.posts.create.PostData
import com.speakout.posts.create.PostsResponse
import com.speakout.utils.AppPreference
import com.speakout.utils.FirebaseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream

/**
 * Created by Kalpesh on 01/08/20.
 */
class PostsRepository(
    private val apiService: ApiService,
    private val appPreference: AppPreference
) : BaseRepository() {

    suspend fun createPost(postData: PostData): Result<PostData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPost(postData)
            if (response.isSuccessful && response.body() != null) {
                return@withContext Result.Success(response.body()!!)
            }
            Result.Error(Exception("Something went wrong"), null)
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(e, null)
        }
    }

    suspend fun uploadImage(bitmap: Bitmap, id: String): String? = withContext(Dispatchers.IO) {
        try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val ref = FirebaseUtils.getPostsStorageRef().child("$id.jpg")
            val uri = ref.putBytes(baos.toByteArray())
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        return@continueWithTask null
                    } else {
                        ref.downloadUrl
                    }
                }.await()
            uri.toString()
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun getProfilePosts(
        userId: String,
        key: Long,
        pageSize: Int
    ): Result<PostsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.getProfilePosts(
                    userId = userId,
                    pageSize = pageSize,
                    key = key
                )
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e, null)
            }
        }

    suspend fun getFeed(key: Long, pageSize: Int): Result<PostsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.getFeed(pageSize = pageSize, key = key)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e, null)
            }
        }

    suspend fun likePost(postMiniDetails: PostMiniDetails): Result<PostMiniDetails> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.likePost(postMiniDetails)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), postMiniDetails)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e, postMiniDetails)
            }
        }

    suspend fun unLikePost(postMiniDetails: PostMiniDetails): Result<PostMiniDetails> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.unLikePost(postMiniDetails)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), postMiniDetails)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e, postMiniDetails)
            }
        }

    suspend fun deletePost(postMiniDetails: PostMiniDetails): Result<PostMiniDetails> =
        withContext(Dispatchers.IO) {
            try {
                FirebaseUtils.getPostsStorageRef()
                    .child("${postMiniDetails.postId}.jpg")
                    .delete()
                val result = apiService.deletePost(postId = postMiniDetails.postId)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Result.Error(e, postMiniDetails)
            }
        }

    suspend fun getSinglePost(postId: String): Result<PostData> = withContext(Dispatchers.IO) {
        try {
            val result = apiService.getSinglePost(postId)
            if (result.isSuccessful && result.body() != null) {
                return@withContext Result.Success(result.body()!!)
            }
            Result.Error(Exception("Something went wrong"), null)
        } catch (e: Exception) {
            Result.Error(e, null)
        }
    }

    suspend fun addBookmark(postId: String, postedBy: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val obj = JsonObject()
                obj.addProperty("postId", postId)
                obj.addProperty("postedBy", postedBy)
                val result = apiService.addBookmark(obj)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(postId)
                }
                Result.Error(Exception("Something went wrong"), postId)
            } catch (e: Exception) {
                Result.Error(e, postId)
            }
        }

    suspend fun removeBookmark(postId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val obj = JsonObject()
            obj.addProperty("postId", postId)
            val result = apiService.removeBookmark(obj)
            if (result.isSuccessful && result.body() != null) {
                return@withContext Result.Success(postId)
            }
            Result.Error(Exception("Something went wrong"), postId)
        } catch (e: Exception) {
            Result.Error(e, postId)
        }
    }

}