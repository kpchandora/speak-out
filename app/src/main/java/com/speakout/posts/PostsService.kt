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
        val ref = FirebaseUtils.getPostsStorageRef().child("$id.jpg")
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
            .orderBy("timeStampLong", Query.Direction.DESCENDING).get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    data.value = emptyList()
                } else {
                    val list = mutableListOf<PostData>()
                    it.forEach { document ->
                        try {
                            val d = document.toObject(PostData::class.java)
                            d.likesSet = d.likes.toHashSet()
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

    fun likePost(postData: PostData): Single<Boolean> {
        return Single.create {

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
            }.addOnCompleteListener { task ->
                Timber.d("likePost: ${postData.content}")
                it.onSuccess(task.isSuccessful)
            }
        }
    }

    fun unlikePost(postData: PostData): Single<Boolean> {
        return Single.create {

            val postRef = getSinglePostRef(postData.postId)

            val postLikesRef = getPostSingleLikeRef(
                postId = postData.postId,
                userId = AppPreference.getUserId()
            )

            getRef().runBatch {
                it.set(postRef, mapOf("likesCount" to FieldValue.increment(-1)), SetOptions.merge())
                it.delete(postLikesRef)
            }.addOnCompleteListener { task ->
                Timber.d("unlikePost: ${postData.content}")
                it.onSuccess(task.isSuccessful)
            }
        }
    }

    fun deletePost(post: PostData): LiveData<Event<Result<PostData>>> {
        val data = MutableLiveData<Event<Result<PostData>>>()
        val userPostRef = getUsersPostRef(postId = post.postId, userId = post.userId)
        val userPostCountRef = getSingleUserRef(post.userId)
        val singlePostRef = getSinglePostRef(post.postId)
        val postLikesRef = getPostLikesRef(post.postId)

        getRef().runBatch {
            it.set(
                userPostCountRef,
                mapOf("postsCount" to FieldValue.increment(-1)),
                SetOptions.merge()
            )
            it.delete(userPostRef)
            it.delete(singlePostRef)
            it.delete(postLikesRef)
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                data.value = Event(Success(post))
            } else {
                data.value = Event(Result.Error(it.exception!!, post))
            }
        }
        return data
    }


    suspend fun getAllPosts(): String = withContext(Dispatchers.IO) {
        try {
            val postId = mapOf("postId" to "aa230665-b59f-4be2-9eec-0236c8147368")
            Timber.d("Task1: ${Looper.getMainLooper() == Looper.myLooper()}")
            val task = FirebaseFunctions.getInstance().getHttpsCallable("getLikesDetails")
                .call(postId)
            Timber.d("Task: ${task.await().data}")
            "Success"
        } catch (e: Exception) {
            "Failure"
        }
    }

}