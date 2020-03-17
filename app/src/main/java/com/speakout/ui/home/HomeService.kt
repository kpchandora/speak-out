package com.speakout.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.NameUtils
import io.reactivex.Single
import timber.log.Timber

object HomeService {

    fun getPosts(): LiveData<List<PostData>> {
        val data = MutableLiveData<List<PostData>>()
        FirebaseUtils.FirestoreUtils.getPostsRef()
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

    private val likeData = MutableLiveData<Pair<Boolean, PostData>>()


    fun likePost(postData: PostData): LiveData<Pair<Boolean, PostData>> {

        val db = FirebaseUtils.FirestoreUtils.getRef()
        val postLikesRef =
            db.document("post_likes/${postData.postId}/users/${AppPreference.getUserId()}")
        val postRef = db.collection(NameUtils.DatabaseRefs.postsRef).document(postData.postId)

        db.runTransaction {
            val newPost =
                it.get(postRef).toObject(PostData::class.java) ?: return@runTransaction null

            it.update(postRef, "likes", FieldValue.arrayUnion(AppPreference.getUserId()))
            it.set(
                postLikesRef,
                PostLikes(
                    username = AppPreference.getUserUniqueName(),
                    photoUrl = AppPreference.getPhotoUrl()
                )
            )
            return@runTransaction newPost
        }.addOnCompleteListener {
            Timber.d("likePost: ${postData.content}")
            if (it.isSuccessful && it.result != null) {
                likeData.value = Pair(true, it.result!!)
            } else {
                likeData.value = Pair(false, postData)
            }
        }

        return likeData
    }

    private val unlikeData = MutableLiveData<Pair<Boolean, PostData>>()

    fun unlikePost(postData: PostData): LiveData<Pair<Boolean, PostData>> {

        val db = FirebaseUtils.FirestoreUtils.getRef()
        val postLikesRef =
            db.document("post_likes/${postData.postId}/users/${AppPreference.getUserId()}")
        val postRef = db.collection(NameUtils.DatabaseRefs.postsRef).document(postData.postId)

        db.runTransaction {
            val newPost =
                it.get(postRef).toObject(PostData::class.java) ?: return@runTransaction null

            it.update(postRef, "likes", FieldValue.arrayRemove(AppPreference.getUserId()))
            it.delete(postLikesRef)
            return@runTransaction newPost
        }.addOnCompleteListener {
            Timber.d("unlikePost: ${postData.content}")
            if (it.isSuccessful && it.result != null) {
                unlikeData.value = Pair(true, it.result!!)
            } else {
                unlikeData.value = Pair(false, postData)
            }
        }

        return unlikeData
    }

    fun unlikePostSingle(postData: PostData): Single<Pair<Boolean, PostData>> {
        return Single.create {
            val db = FirebaseUtils.FirestoreUtils.getRef()
            val postLikesRef =
                db.document("post_likes/${postData.postId}/users/${AppPreference.getUserId()}")
            val postRef = db.collection(NameUtils.DatabaseRefs.postsRef).document(postData.postId)

            db.runTransaction {
                val newPost =
                    it.get(postRef).toObject(PostData::class.java) ?: return@runTransaction null

                it.update(postRef, "likes", FieldValue.arrayRemove(AppPreference.getUserId()))
                it.delete(postLikesRef)
                return@runTransaction newPost
            }.addOnCompleteListener { task ->
                Timber.d("unlikePost: ${postData.content}")
                if (task.isSuccessful && task.result != null) {
                    it.onSuccess(Pair(true, task.result!!))
                } else {
                    it.onSuccess(Pair(false, postData))
                }
            }
        }
    }

}