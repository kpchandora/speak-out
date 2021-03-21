package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.common.Event
import com.speakout.posts.create.PostData
import com.speakout.common.Result
import com.speakout.posts.PostMiniDetails
import com.speakout.posts.PostsRepository
import com.speakout.posts.create.PostsResponse
import com.speakout.utils.AppPreference
import kotlinx.coroutines.launch

class HomeViewModel(
    private val appPreference: AppPreference,
    private val postsRepository: PostsRepository
) : ViewModel() {

    companion object {
        const val FEED_POSTS_COUNT = 10
        const val PROFILE_POSTS_COUNT = 20
    }

    val mPostList = ArrayList<PostData>()

    private val _unlikePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val unlikePost: LiveData<Event<Result<PostMiniDetails>>> = _unlikePost

    private val _likePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val likePost: LiveData<Event<Result<PostMiniDetails>>> = _likePost

    private val _deletePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val deletePost: LiveData<Event<Result<PostMiniDetails>>> = _deletePost

    private val _singlePost = MutableLiveData<Event<Result<PostData>>>()
    val singlePost: LiveData<Event<Result<PostData>>> = _singlePost

    private val _posts = MutableLiveData<PostsResponse>()
    val posts: LiveData<PostsResponse> = _posts

    private val _addBookmark = MutableLiveData<Event<Result<String>>>()
    val addBookmark: LiveData<Event<Result<String>>> = _addBookmark

    private val _removeBookmark = MutableLiveData<Event<Result<String>>>()
    val removeBookmark: LiveData<Event<Result<String>>> = _removeBookmark

    private val _postsError = MutableLiveData<Event<String>>()
    val postsError: LiveData<Event<String>> = _postsError

    fun getProfilePosts(id: String, key: Long) {
        viewModelScope.launch {
            val response = postsRepository.getProfilePosts(
                userId = id,
                key = key,
                pageSize = PROFILE_POSTS_COUNT
            )
            if (response is Result.Success) {
                mPostList.addAll(response.data.posts)
                _posts.value = response.data
            }
            if (response is Result.Error) {
                _postsError.value = Event(response.error.message!!)
            }
        }
    }

    fun getFeed(key: Long) {
        viewModelScope.launch {
            val response = postsRepository.getFeed(
                key = key,
                pageSize = FEED_POSTS_COUNT
            )
            if (response is Result.Success) {
                mPostList.addAll(response.data.posts)
                _posts.value = response.data
            }
            if (response is Result.Error) {
                _postsError.value = Event(response.error.message!!)
            }
        }
    }

    fun likePost(postData: PostData) {
        viewModelScope.launch {
            _likePost.value = Event(
                postsRepository.likePost(
                    PostMiniDetails(postId = postData.postId, userId = appPreference.getUserId())
                )
            )
        }
    }

    fun unlikePost(postData: PostData) {
        viewModelScope.launch {
            _unlikePost.value = Event(
                postsRepository.unLikePost(
                    PostMiniDetails(postId = postData.postId, userId = appPreference.getUserId())
                )
            )
        }
    }


    fun deletePost(postData: PostData) {
        viewModelScope.launch {
            _deletePost.value = Event(
                postsRepository.deletePost(
                    PostMiniDetails(postId = postData.postId, userId = appPreference.getUserId())
                )
            )
        }
    }

    fun getSinglePost(postId: String) {
        viewModelScope.launch {
            _singlePost.value = Event(postsRepository.getSinglePost(postId))
        }
    }

    fun addBookmark(postId: String, postedBy: String) {
        viewModelScope.launch {
            _addBookmark.value = Event(postsRepository.addBookmark(postId, postedBy))
        }
    }

    fun removeBookmark(postId: String) {
        viewModelScope.launch {
            _removeBookmark.value = Event(postsRepository.removeBookmark(postId))
        }
    }

}