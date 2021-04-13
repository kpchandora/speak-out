package com.speakoutall.posts.create

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.common.Event
import com.speakoutall.posts.PostsRepository
import kotlinx.coroutines.launch
import com.speakoutall.common.Result
import com.speakoutall.utils.AppPreference

class CreatePostViewModel : ViewModel() {
    private val appPreference = AppPreference
    private val mPostsRepository: PostsRepository by lazy {
        PostsRepository(RetrofitBuilder.apiService, appPreference)
    }
    private val _post = MutableLiveData<Event<Result<PostData>>>()
    val createPost: LiveData<Event<Result<PostData>>> = _post
    private val _uploadImage = MutableLiveData<Event<String?>>()
    val uploadImage: LiveData<Event<String?>> = _uploadImage
    val tags = MutableLiveData<List<String>>()
    var imageBitmap: Bitmap? = null

    fun createPost(postData: PostData) {
        viewModelScope.launch {
            _post.value = Event(mPostsRepository.createPost(postData))
        }
    }

    fun uploadImage(bitmap: Bitmap, postId: String) {
        viewModelScope.launch {
            _uploadImage.value = Event(mPostsRepository.uploadImage(bitmap, postId))
        }
    }

}