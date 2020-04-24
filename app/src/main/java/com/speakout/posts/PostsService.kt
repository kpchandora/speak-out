package com.speakout.posts

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.speakout.common.Event
import com.speakout.common.Result.Success
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import com.speakout.common.Result
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getAllPostsRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getPostLikesRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getPostSingleLikeRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getSinglePostRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getSingleUserRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getUsersPostRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getUsersRef
import com.speakout.utils.FirebaseUtils.getFirebaseFunction
import com.speakout.utils.FirebaseUtils.getPostsStorageRef
import com.speakout.utils.NameUtils
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream

object PostsService {

    fun createPost(postData: PostData): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        val postRef = getSinglePostRef(postData.postId)
        val userPostsRef = getUsersRef().document(postData.userId)
            .collection(NameUtils.DatabaseRefs.postsRef)
            .document(postData.postId)

        val postCountRef = getUsersRef().document(postData.userId)

        getRef().runBatch {
            it.set(
                postCountRef,
                mapOf("postsCount" to FieldValue.increment(1)),
                SetOptions.merge()
            )
            it.set(postRef, postData)
            it.set(userPostsRef, mapOf("timeStamp" to System.currentTimeMillis()))
        }.addOnCompleteListener {
            data.value = it.isSuccessful
        }

        return data
    }

    fun uploadImage(bitmap: Bitmap, id: String): LiveData<String?> {
        val data = MutableLiveData<String>()
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val ref = getPostsStorageRef().child("$id.jpg")
        ref.putBytes(baos.toByteArray())
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    data.value = null
                    return@continueWithTask null
                } else {
                    ref.downloadUrl
                }
            }.addOnCompleteListener {
                if (it.isSuccessful) {
                    data.value = it.result?.toString()
                } else {
                    data.value = null
                }
            }
        return data
    }


    fun getPosts(userId: String): LiveData<List<PostData>> {
        val data = MutableLiveData<List<PostData>>()
        getAllPostsRef()
            .whereEqualTo("userId", userId)
            .orderBy("timeStampLong", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    data.value = emptyList()
                } else {
                    val list = mutableListOf<PostData>()
                    it.forEach { document ->
                        try {
                            val d = document.toObject(PostData::class.java)
                            list.add(d)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    data.value = list
                }
            }.addOnFailureListener {
                data.value = emptyList()
            }

        return data
    }

    suspend fun likePostNew(postData: PostData): Result<PostData> = withContext(Dispatchers.IO) {
        try {
            val postRef = getSinglePostRef(postData.postId)

            val postLikesRef = getPostSingleLikeRef(
                postId = postData.postId,
                userId = AppPreference.getUserId()
            )
            getRef().runBatch {
                it.set(postRef, mapOf("likesCount" to FieldValue.increment(1)), SetOptions.merge())

                it.set(
                    postLikesRef,
                    mapOf("timeStamp" to System.currentTimeMillis())
                )
            }.await()
            Success(postData)
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(e, postData)
        }
    }

    suspend fun unlikePostNew(postData: PostData): Result<PostData> = withContext(Dispatchers.IO) {
        try {
            val postRef = getSinglePostRef(postData.postId)

            val postLikesRef = getPostSingleLikeRef(
                postId = postData.postId,
                userId = AppPreference.getUserId()
            )

            getRef().runBatch {
                it.set(postRef, mapOf("likesCount" to FieldValue.increment(-1)), SetOptions.merge())
                it.delete(postLikesRef)
            }.await()

            Success(postData)
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(e, postData)
        }
    }

    suspend fun deletePost(post: PostData): Event<Result<PostData>> =
        withContext(Dispatchers.IO) {
            try {
                val userPostRef = getUsersPostRef(postId = post.postId, userId = post.userId)
                val userPostCountRef = getSingleUserRef(post.userId)
                val singlePostRef = getSinglePostRef(post.postId)
                val postLikesRef = getPostLikesRef(post.postId)
                val postStorageRef = getPostsStorageRef().child("${post.postId}.jpg")

                getRef().runBatch {
                    it.set(
                        userPostCountRef,
                        mapOf("postsCount" to FieldValue.increment(-1)),
                        SetOptions.merge()
                    )
                    it.delete(userPostRef)
                    it.delete(singlePostRef)
                    it.delete(postLikesRef)
                }.await()

                postStorageRef.delete().await()

                Event(Success(post))
            } catch (e: Exception) {
                Timber.e(e)
                Event(Result.Error(e, post))
            }
        }

    suspend fun getProfilePosts(userId: String): Result<List<PostData>> =
        withContext(Dispatchers.IO) {
            try {
                val data = mapOf("userId" to userId)
                val result = getFirebaseFunction("getProfilePosts").call(data).await()
                val json = Gson().toJson(result.data)
                val list = Gson().fromJson(json, Array<PostData>::class.java).asList()
                Timber.d("Posts List: ${list.size}")
                Success(list)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e, null)
            }
        }
}