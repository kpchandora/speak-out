package com.speakoutall.ui.profile

import androidx.lifecycle.*
import com.speakoutall.auth.UserDetails
import com.speakoutall.common.Event
import com.speakoutall.common.Result
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.ImageUtils
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(private val appPreference: AppPreference,
                       private val mUsersRepository: UsersRepository) : ViewModel() {

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