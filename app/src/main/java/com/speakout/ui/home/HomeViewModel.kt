package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.create.PostData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers

class HomeViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _posts = MutableLiveData<String>()
    val posts: LiveData<List<PostData>> = Transformations.switchMap(_posts) {
        HomeService.getPosts()
    }

    private val _likePost = MutableLiveData<PostData>()
    val likePost: LiveData<Pair<Boolean, PostData>> = Transformations.switchMap(_likePost) {
        HomeService.likePost(it)
    }

//    private val _unlikePost = MutableLiveData<PostData>()
//    val unlikePost: LiveData<Pair<Boolean, PostData>> = Transformations.switchMap(_unlikePost) {
//        HomeService.unlikePost(postData = it)
//    }

    fun getPosts(id: String) {
        _posts.value = id
    }

    fun likePost(postData: PostData) {
        _likePost.value = postData
    }

//    fun unlikePost(postData: PostData) {
//        _unlikePost.value = postData
//    }


    //Single test
    private val _unlikePost = MutableLiveData<Pair<Boolean, PostData>>()
    val unlikePost: LiveData<Pair<Boolean, PostData>> = _unlikePost

    fun unlikePost(postData: PostData) {
        compositeDisposable += HomeService.unlikePostSingle(postData)
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