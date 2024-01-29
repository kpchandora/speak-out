package com.speakoutall.auth

import androidx.lifecycle.*
import com.speakoutall.common.Event
import com.speakoutall.users.UsersRepository
import com.speakoutall.common.Result
import kotlinx.coroutines.launch

class UserViewModel(private val mUsersRepository: UsersRepository) : ViewModel() {

    private val _username = MutableLiveData<Result<Boolean>>()
    val username: LiveData<Result<Boolean>> = _username

    private val _updateUserDetails = MutableLiveData<Event<Result<UserDetails>>>()
    val updateUserDetails: LiveData<Event<Result<UserDetails>>> = _updateUserDetails
    private val _userDetails = MutableLiveData<Result<UserDetails>>()
    val userDetails: LiveData<Result<UserDetails>> = _userDetails

    fun saveUserDetails(userDetails: UserDetails, isNewUser: Boolean) {
        viewModelScope.launch {
            _userDetails.value = mUsersRepository.createUser(userDetails, isNewUser)
        }
    }

    fun updateUserDetails(userMiniDetails: UsersItem) {
        viewModelScope.launch {
            _updateUserDetails.value = Event(mUsersRepository.updateUserDetails(userMiniDetails))
        }
    }

    fun isUsernamePresent(username: String) {
        viewModelScope.launch {
            _username.value = mUsersRepository.checkUsername(username)
        }
    }
}