package com.speakout.ui.bookmark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakout.common.Event
import com.speakout.common.Result
import com.speakout.posts.PostsRepository
import com.speakout.posts.create.PostData
import com.speakout.posts.create.PostsResponse
import com.speakout.ui.home.HomeViewModel
import kotlinx.coroutines.launch

class BookmarksViewModel(private val postsRepository: PostsRepository) : ViewModel() {

    companion object {
        const val POSTS_COUNT = 20
    }

    private val _posts = MutableLiveData<PostsResponse>()
    val posts: LiveData<PostsResponse> = _posts

    private val _postsError = MutableLiveData<Event<String>>()
    val postsError: LiveData<Event<String>> = _postsError

    val mPostList = ArrayList<PostData>()

    fun getBookmarks(key: Long) {
        viewModelScope.launch {
            val response = postsRepository.getBookmarks(
                key = key,
                pageSize = POSTS_COUNT
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

}