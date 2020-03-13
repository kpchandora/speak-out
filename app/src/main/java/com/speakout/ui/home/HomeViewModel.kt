package com.speakout.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.speakout.posts.create.PostData

class HomeViewModel : ViewModel() {

    private val _posts = MutableLiveData<String>()
    val posts: LiveData<List<PostData>> = Transformations.switchMap(_posts) {
        HomeService.getPosts()
    }

    private val _likePost = MutableLiveData<PostData>()
    val likePost: LiveData<PostData?> = Transformations.switchMap(_likePost) {
        HomeService.likePost(it)
    }

    fun getPosts(id: String) {
        _posts.value = id
    }

    fun likePost(postData: PostData) {
        _likePost.value = postData
    }

}