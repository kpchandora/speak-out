package com.speakout.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.speakout.common.Event
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getAllPostsRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getSinglePostRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getSingleUserRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getUsersPostRef
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getUsersRef
import io.reactivex.Single
import timber.log.Timber

object PostsService {

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

            val postLikesRef = FirebaseUtils.FirestoreUtils.getPostLikesRef(
                postId = postData.postId,
                userId = AppPreference.getUserId()
            )

            FirebaseUtils.FirestoreUtils.getRef().runTransaction {
                it.set(postRef, mapOf("likesCount" to FieldValue.increment(1)), SetOptions.merge())

                it.set(
                    postLikesRef,
                    mapOf("timeStamp" to System.currentTimeMillis())
                )
                true
            }.addOnCompleteListener { task ->
                Timber.d("likePost: ${postData.content}")
                if (task.isSuccessful && task.result != null) {
                    it.onSuccess(task.result ?: false)
                } else {
                    it.onSuccess(false)
                }
            }

        }
    }

    fun unlikePost(postData: PostData): Single<Boolean> {
        return Single.create {

            val postRef = getSinglePostRef(postData.postId)

            val postLikesRef = FirebaseUtils.FirestoreUtils.getPostLikesRef(
                postId = postData.postId,
                userId = AppPreference.getUserId()
            )

            FirebaseUtils.FirestoreUtils.getRef().runTransaction {
                it.set(postRef, mapOf("likesCount" to FieldValue.increment(-1)), SetOptions.merge())
                it.delete(postLikesRef)
                true
            }.addOnCompleteListener { task ->
                Timber.d("unlikePost: ${postData.content}")
                if (task.isSuccessful && task.result != null) {
                    it.onSuccess(true)
                } else {
                    it.onSuccess(true)
                }
            }
        }
    }

    fun deletePost(post: PostData): LiveData<Event<Boolean>> {
        val data = MutableLiveData<Event<Boolean>>()

        val userPostRef = getUsersPostRef(postId = post.postId, userId = post.userId)
        val userPostCountRef = getSingleUserRef(post.userId)
        val singlePostRef = getSinglePostRef(post.postId)

        getRef().runBatch {
            it.set(
                userPostCountRef,
                mapOf("postsCount" to FieldValue.increment(-1)),
                SetOptions.merge()
            )
            it.delete(userPostRef)
            it.delete(singlePostRef)
        }.addOnCompleteListener {
            data.value = Event(it.isSuccessful)
        }
        return data
    }

}