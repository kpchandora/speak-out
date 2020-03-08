package com.speakout.posts.create

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class CreatePostViewModel : ViewModel() {

    private val post = MutableLiveData<CreatePostData>()
    private val uploadImage = MutableLiveData<Pair<Bitmap, String>>()
    val tags = MutableLiveData<List<String>>()

    val uploadImageObserver: LiveData<String?> = Transformations.switchMap(uploadImage) {
        CreatePostService.uploadImage(bitmap = it.first, id = it.second)
    }

    val postObserver: LiveData<Boolean> = Transformations.switchMap(post) {
        CreatePostService.createPost(postData = it)
    }

    fun uploadImage(pair: Pair<Bitmap, String>) {
        uploadImage.value = pair
    }

    fun uploadPost(postData: CreatePostData) {
        post.value = postData
    }

}