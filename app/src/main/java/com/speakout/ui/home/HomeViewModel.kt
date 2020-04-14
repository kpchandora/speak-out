package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.PostsService
import com.speakout.posts.create.PostData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class HomeViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mPostList = ArrayList<PostData>()

    private val _unlikePost = MutableLiveData<Boolean>()
    val unlikePost: LiveData<Boolean> = _unlikePost

    private val _likePost = MutableLiveData<Boolean>()
    val likePost: LiveData<Boolean> = _likePost


    private val _posts = MutableLiveData<String>()
    val posts: LiveData<List<PostData>> = Transformations.switchMap(_posts) {
        PostsService.getPosts(it)
    }

    fun getPosts(id: String) {
        _posts.value = id
    }

    fun likePost(postData: PostData) {
        compositeDisposable += PostsService.likePost(postData)
            .withDefaultSchedulers()
            .subscribe({
                _likePost.value = it
            }, {
                _likePost.value = false
            })
    }

    fun unlikePost(postData: PostData) {
        compositeDisposable += PostsService.unlikePost(postData)
            .withDefaultSchedulers()
            .subscribe({
                _unlikePost.value = it
            }, {
                _unlikePost.value = false
            })
    }

    fun addPosts(list: List<PostData>) {
        mPostList.addAll(list)
    }

    fun getPosts() = mPostList

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}