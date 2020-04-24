package com.speakout.ui.home

import android.os.Looper
import androidx.lifecycle.*
import com.speakout.common.Event
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.PostsService
import com.speakout.posts.create.PostData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.speakout.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeViewModel : ViewModel() {

    private val bgScope = viewModelScope.coroutineContext + Dispatchers.IO

    private val compositeDisposable = CompositeDisposable()
    private val mPostList = ArrayList<PostData>()

    private val _unlikePost = MutableLiveData<Event<Result<PostData>>>()
    val unlikePost: LiveData<Event<Result<PostData>>> = _unlikePost

    private val _likePost = MutableLiveData<Event<Result<PostData>>>()
    val likePost: LiveData<Event<Result<PostData>>> = _likePost

    private val _deletePost = MutableLiveData<Event<Result<PostData>>>()
    val deletePost: LiveData<Event<Result<PostData>>> = _deletePost


    private val _posts = MutableLiveData<Result<List<PostData>>>()
    val posts: LiveData<Result<List<PostData>>> = _posts

    fun getPosts(id: String) {
        viewModelScope.launch {
            _posts.value = PostsService.getProfilePosts(id)
        }
    }

    fun likePost(postData: PostData) {
        viewModelScope.launch {
            _likePost.value = Event(PostsService.likePostNew(postData))
        }
    }

    fun unlikePost(postData: PostData) {
        viewModelScope.launch {
            _unlikePost.value = Event(PostsService.unlikePostNew(postData))
        }
    }

    fun addPosts(list: List<PostData>) {
        mPostList.addAll(list)
    }

    fun getPosts() = mPostList

    fun deletePost(postData: PostData) {
        viewModelScope.launch {
            _deletePost.value = PostsService.deletePost(postData)
        }
    }


    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}