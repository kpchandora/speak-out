package com.speakout.posts

import android.graphics.Bitmap
import com.speakout.api.ApiService
import com.speakout.common.Result
import com.speakout.posts.create.PostData
import com.speakout.utils.FirebaseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.sql.Time

/**
 * Created by Kalpesh on 01/08/20.
 */
public class PostsRepository(private val apiService: ApiService) {

    suspend fun createPost(postData: PostData): Result<PostData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPost(postData)
            if (response.isSuccessful && response.body() != null) {
                return@withContext Result.Success(response.body()!!)
            }
            Result.Error<PostData>(Exception("Something went wrong"))
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error<PostData>(e)
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
}