package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.posts.create.PostData

class HomeViewModel : ViewModel() {

    private val _posts = MutableLiveData<String>()
    val posts: LiveData<List<PostData>> = Transformations.switchMap(_posts) {
        HomeService.getPosts()
    }

    private val _likePost = MutableLiveData<PostData>()
    val likePost: LiveData<Pair<Boolean, PostData>> = Transformations.switchMap(_likePost) {
        HomeService.likePost(it)
    }

    private val _unlikePost = MutableLiveData<PostData>()
    val unlikePost: LiveData<Pair<Boolean, PostData>> = Transformations.switchMap(_unlikePost) {
        HomeService.unlikePost(postData = it)
    }

    fun getPosts(id: String) {
        _posts.value = id
    }

    fun likePost(postData: PostData) {
        _likePost.value = postData
    }

    fun unlikePost(postData: PostData) {
        _unlikePost.value = postData
    }

}