package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class HomeViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _unlikePost = MutableLiveData<Boolean>()
    val unlikePost: LiveData<Boolean> = _unlikePost

    private val _likePost = MutableLiveData<Boolean>()
    val likePost: LiveData<Boolean> = _likePost


    private val _posts = MutableLiveData<String>()
    val posts: LiveData<List<PostData>> = Transformations.switchMap(_posts) {
        HomeService.getPosts(it)
    }

    fun getPosts(id: String) {
        _posts.value = id
    }

    fun likePost(postData: PostData) {
        compositeDisposable += HomeService.newLikePost(postData)
            .withDefaultSchedulers()
            .subscribe({
                _likePost.value = it
            }, {
                _likePost.value = false
            })
    }

    fun unlikePost(postData: PostData) {
        compositeDisposable += HomeService.newUnlikePost(postData)
            .withDefaultSchedulers()
            .subscribe({
                _unlikePost.value = it
            }, {
                _unlikePost.value = false
            })
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}