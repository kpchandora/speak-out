package com.speakout.posts.create

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.NameUtils
import java.io.ByteArrayOutputStream

object CreatePostService {

    fun createPost(postData: PostData): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        val postRef = FirebaseUtils.FirestoreUtils.getSinglePostRef(postData.postId)
        val userPostsRef = FirebaseUtils.FirestoreUtils.getUsersRef().document(postData.userId)
            .collection(NameUtils.DatabaseRefs.postsRef)
            .document(postData.postId)

        val postCountRef = FirebaseUtils.FirestoreUtils.getUsersRef().document(postData.userId)

        FirebaseUtils.FirestoreUtils.getRef().runBatch {
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

}