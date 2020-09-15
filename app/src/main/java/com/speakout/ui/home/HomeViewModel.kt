package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.api.RetrofitBuilder
import com.speakout.common.Event
import com.speakout.posts.create.PostData
import io.reactivex.disposables.CompositeDisposable
import com.speakout.common.Result
import com.speakout.posts.PostMiniDetails
import com.speakout.posts.PostsRepository
import com.speakout.utils.AppPreference
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val appPreference = AppPreference
    private val mPostsRepository: PostsRepository by lazy {
        PostsRepository(RetrofitBuilder.apiService, appPreference)
    }

    private val mPostList = ArrayList<PostData>()

    private val _unlikePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val unlikePost: LiveData<Event<Result<PostMiniDetails>>> = _unlikePost

    private val _likePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val likePost: LiveData<Event<Result<PostMiniDetails>>> = _likePost

    private val _deletePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val deletePost: LiveData<Event<Result<PostMiniDetails>>> = _deletePost

    private val _singlePost = MutableLiveData<Event<Result<PostData>>>()
    val singlePost: LiveData<Event<Result<PostData>>> = _singlePost

    private val _posts = MutableLiveData<Event<Result<List<PostData>>>>()
    val posts: LiveData<Event<Result<List<PostData>>>> = _posts

    fun getPosts(id: String) {
        viewModelScope.launch {
            _posts.value = Event(mPostsRepository.getProfilePosts(id))
        }
    }

    fun getFeed() {
        viewModelScope.launch {
            _posts.value = Event(mPostsRepository.getFeed())
        }
    }

    fun likePost(postData: PostData) {
        viewModelScope.launch {
            _likePost.value = Event(
                mPostsRepository.likePost(
                    PostMiniDetails(postId = postData.postId, userId = appPreference.getUserId())
                )
            )
        }
    }

    fun unlikePost(postData: PostData) {
        viewModelScope.launch {
            _unlikePost.value = Event(
                mPostsRepository.unLikePost(
                    PostMiniDetails(postId = postData.postId, userId = appPreference.getUserId())
                )
            )
        }
    }

    fun addPosts(list: List<PostData>) {
        mPostList.addAll(list)
    }

    fun getPosts() = mPostList

    fun deletePost(postData: PostData) {
        viewModelScope.launch {
            _deletePost.value = Event(
                mPostsRepository.deletePost(
                    PostMiniDetails(postId = postData.postId, userId = appPreference.getUserId())
                )
            )
        }
    }

    fun getSinglePost(postId: String) {
        viewModelScope.launch {
            _singlePost.value = Event(mPostsRepository.getSinglePost(postId))
        }
    }

}