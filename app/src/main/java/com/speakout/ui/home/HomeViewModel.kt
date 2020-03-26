package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class HomeViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _unlikePost = MutableLiveData<Pair<Boolean, PostData>>()
    val unlikePost: LiveData<Pair<Boolean, PostData>> = _unlikePost

    private val _likePost = MutableLiveData<Pair<Boolean, PostData>>()
    val likePost: LiveData<Pair<Boolean, PostData>> = _likePost


    private val _posts = MutableLiveData<String>()
    val posts: LiveData<List<PostData>> = Transformations.switchMap(_posts) {
        HomeService.getPosts(AppPreference.getUserId())
    }

    fun getPosts(id: String) {
        _posts.value = id
    }

    fun likePost(postData: PostData) {
        compositeDisposable += HomeService.likePost(postData)
            .withDefaultSchedulers()
            .subscribe({
                _likePost.value = it
            }, {
                _likePost.value = Pair(false, postData)
            })
    }

    fun unlikePost(postData: PostData) {
        compositeDisposable += HomeService.unlikePost(postData)
            .withDefaultSchedulers()
            .subscribe({
                _unlikePost.value = it
            }, {
                _unlikePost.value = Pair(false, postData)
            })
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}