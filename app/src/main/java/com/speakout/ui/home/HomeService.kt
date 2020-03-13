package com.speakout.ui.home

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.NameUtils
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
                            list.add(document.toObject(PostData::class.java))
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

    fun likePost(postData: PostData): LiveData<PostData?> {
        val data = MutableLiveData<PostData>()

        val db = FirebaseUtils.FirestoreUtils.getRef()
        val postLikesRef =
            db.document("post_likes/${postData.postId}/users/${AppPreference.getUserId()}")
        val postRef = db.collection(NameUtils.DatabaseRefs.postsRef).document(postData.postId)

        db.runTransaction {
            val newPost =
                it.get(postRef).toObject(PostData::class.java) ?: return@runTransaction null

            Timber.d("Data: $newPost")

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
            data.value = it.result
        }

        return data
    }

}