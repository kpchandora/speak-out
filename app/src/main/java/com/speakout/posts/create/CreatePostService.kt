package com.speakout.posts.create

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.NameUtils
import java.io.ByteArrayOutputStream

object CreatePostService {

    fun createPost(postData: CreatePostData): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.FirestoreUtils.getPostsRef().document(postData.postId).set(postData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    FirebaseUtils.FirestoreUtils.getUsersRef()
                        .document(postData.userId)
                        .collection(NameUtils.DatabaseRefs.postsRef)
                        .document(postData.postId)
                        .set(mapOf(postData.postId to postData.postId))
                        .addOnCompleteListener {
                            data.value = it.isSuccessful
                        }

                } else {
                    data.value = false
                }
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