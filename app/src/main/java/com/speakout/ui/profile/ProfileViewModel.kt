package com.speakout.ui.profile

import androidx.lifecycle.*
import com.speakout.api.RetrofitBuilder
import com.speakout.auth.UserDetails
import com.speakout.common.Event
import com.speakout.common.Result
import com.speakout.posts.create.PostData
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import com.speakout.utils.ImageUtils
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel : ViewModel() {
    private val appPreference = AppPreference
    private val mUsersRepository: UsersRepository by lazy {
        UsersRepository(RetrofitBuilder.apiService, appPreference)
    }

    private val compositeDisposable = CompositeDisposable()
    private val mPostList = ArrayList<PostData>()

    private val _confirmUnfollow = MutableLiveData<Event<Unit>>()
    val confirmUnfollow: LiveData<Event<Unit>> = _confirmUnfollow

    private val _userDetails = MutableLiveData<Result<UserDetails>>()
    val userDetails: LiveData<Result<UserDetails>> = _userDetails

    private val _followUser = MutableLiveData<Event<Result<UserDetails>>>()
    val followUser: LiveData<Event<Result<UserDetails>>>
        get() = _followUser

    private val _unFollowUser = MutableLiveData<Event<Result<UserDetails>>>()
    val unFollowUser: LiveData<Event<Result<UserDetails>>>
        get() = _unFollowUser

    private val _uploadProfilePicture = MutableLiveData<Event<Result<String>>>()
    val uploadProfilePicture: LiveData<Event<Result<String>>>
        get() = _uploadProfilePicture

    private val _posts = MutableLiveData<Result<List<PostData>>>()
    val posts: LiveData<Result<List<PostData>>> = _posts


    fun uploadProfilePicture(imageFile: File) {
        viewModelScope.launch {
            _uploadProfilePicture.value = Event(ImageUtils.uploadImageFromFile(imageFile))
        }
    }

    fun getUser(uid: String) {
        viewModelScope.launch {
            _userDetails.value = mUsersRepository.getUser(uid)
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            _followUser.value = Event(mUsersRepository.followUser(userId = userId))
        }
    }

    fun unFollowUser(userId: String) {
        viewModelScope.launch {
            _unFollowUser.value = Event(mUsersRepository.unFollowUser(userId = userId))
        }
    }

    fun confirmUnfollow() {
        _confirmUnfollow.value = Event(Unit)
    }

}