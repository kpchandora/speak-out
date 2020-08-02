package com.speakout.posts

import android.graphics.Bitmap
import com.speakout.api.ApiService
import com.speakout.common.Result
import com.speakout.posts.create.PostData
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
public class PostsRepository(
    private val apiService: ApiService,
    private val appPreference: AppPreference
) {

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

    suspend fun getProfilePosts(userId: String): Result<List<PostData>> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.getProfilePosts(appPreference.getUserId(), userId)
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
                    .delete().await()
                val result = apiService.deletePost(
                    selfUserId = postMiniDetails.userId,
                    postId = postMiniDetails.postId
                )
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Result.Error(e, postMiniDetails)
            }
        }

}