package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.common.Event
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.PostsService
import com.speakout.posts.create.PostData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class HomeViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mPostList = ArrayList<PostData>()

    private val _unlikePost = MutableLiveData<Event<Boolean>>()
    val unlikePost: LiveData<Event<Boolean>> = _unlikePost

    private val _likePost = MutableLiveData<Event<Boolean>>()
    val likePost: LiveData<Event<Boolean>> = _likePost

    private val _deletePost = MutableLiveData<PostData>()
    val deletePost: LiveData<Event<Boolean>> = _deletePost.switchMap {
        PostsService.deletePost(it)
    }

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
                _likePost.value = Event(it)
            }, {
                _likePost.value = Event(false)
            })
    }

    fun unlikePost(postData: PostData) {
        compositeDisposable += PostsService.unlikePost(postData)
            .withDefaultSchedulers()
            .subscribe({
                _unlikePost.value = Event(it)
            }, {
                _unlikePost.value = Event(false)
            })
    }

    fun addPosts(list: List<PostData>) {
        mPostList.addAll(list)
    }

    fun getPosts() = mPostList

    fun deletePost(postData: PostData) {
        _deletePost.value = postData
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}