package com.speakout.posts.create

import android.graphics.Bitmap
import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.data.FreezableUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.ImageUtils
import com.speakout.utils.StringUtils
import io.reactivex.Single
import java.io.ByteArrayOutputStream

object CreatePostService {

    fun createPost(postData: CreatePostData): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseUtils.getReference().child(StringUtils.DatabaseRefs.postsRef).child(postData.postId)
            .setValue(postData).addOnCompleteListener {
                data.value = it.isSuccessful

                FirebaseUtils.getReference().child(StringUtils.DatabaseRefs.userDetailsRef)
                    .child(postData.userId).child(StringUtils.DatabaseRefs.postsRef)
                    .child(postData.postId).setValue(postData.postId)
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