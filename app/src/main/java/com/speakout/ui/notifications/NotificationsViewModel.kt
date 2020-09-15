package com.speakout.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakout.api.RetrofitBuilder
import com.speakout.common.Event
import com.speakout.common.Result
import com.speakout.posts.PostsRepository
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val appPreference = AppPreference
    private val mPostsRepository: PostsRepository by lazy {
        PostsRepository(RetrofitBuilder.apiService, appPreference)
    }

    private val _singlePost = MutableLiveData<Event<Result<PostData>>>()
    val singlePost: LiveData<Event<Result<PostData>>> = _singlePost

    fun getPost(postId: String) {
        viewModelScope.launch {
            _singlePost.value = Event(mPostsRepository.getSinglePost(postId))
        }
    }

}