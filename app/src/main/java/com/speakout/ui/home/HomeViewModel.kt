package com.speakout.ui.home

import androidx.lifecycle.*
import com.speakout.api.RetrofitBuilder
import com.speakout.common.Event
import com.speakout.posts.create.PostData
import com.speakout.common.Result
import com.speakout.posts.PostMiniDetails
import com.speakout.posts.PostsRepository
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

    private var feedPageCount = 0
    private var profilePageCount = 0
    private val mPostList = ArrayList<PostData>()

    private val _unlikePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val unlikePost: LiveData<Event<Result<PostMiniDetails>>> = _unlikePost

    private val _likePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val likePost: LiveData<Event<Result<PostMiniDetails>>> = _likePost

    private val _deletePost = MutableLiveData<Event<Result<PostMiniDetails>>>()
    val deletePost: LiveData<Event<Result<PostMiniDetails>>> = _deletePost

    private val _singlePost = MutableLiveData<Event<Result<PostData>>>()
    val singlePost: LiveData<Event<Result<PostData>>> = _singlePost

    private val _posts = MutableLiveData<Result<List<PostData>>>()
    val posts: LiveData<Result<List<PostData>>> = _posts

    private val _addBookmark = MutableLiveData<Event<Result<String>>>()
    val addBookmark: LiveData<Event<Result<String>>> = _addBookmark

    private val _removeBookmark = MutableLiveData<Event<Result<String>>>()
    val removeBookmark: LiveData<Event<Result<String>>> = _removeBookmark

    fun getProfilePosts(id: String) {
        viewModelScope.launch {
            profilePageCount = 1
            _posts.value = postsRepository.getProfilePosts(
                userId = id,
                pageNumber = profilePageCount,
                pageSize = PROFILE_POSTS_COUNT
            )
        }
    }

    fun loadMoreProfilePosts(id: String) {
        viewModelScope.launch {
            profilePageCount++
            _posts.value = postsRepository.getProfilePosts(
                userId = id,
                pageNumber = profilePageCount,
                pageSize = PROFILE_POSTS_COUNT
            )
        }
    }

    fun getFeed() {
        viewModelScope.launch {
            feedPageCount = 1
            _posts.value = postsRepository.getFeed(feedPageCount, FEED_POSTS_COUNT)
        }
    }

    fun loadMoreFeed() {
        viewModelScope.launch {
            feedPageCount++
            _posts.value = postsRepository.getFeed(feedPageCount, FEED_POSTS_COUNT)
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

    fun addPosts(list: List<PostData>) {
        mPostList.addAll(list)
    }

    fun getProfilePosts() = mPostList

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

    fun addBookmark(postId: String) {
        viewModelScope.launch {
            _addBookmark.value = Event(postsRepository.addBookmark(postId))
        }
    }

    fun removeBookmark(postId: String) {
        viewModelScope.launch {
            _removeBookmark.value = Event(postsRepository.removeBookmark(postId))
        }
    }

}