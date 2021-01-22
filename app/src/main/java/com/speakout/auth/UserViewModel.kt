package com.speakout.auth

import androidx.lifecycle.*
import com.speakout.api.RetrofitBuilder
import com.speakout.common.Event
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import com.speakout.common.Result
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val appPreference = AppPreference
    private val mUsersRepository: UsersRepository by lazy {
        UsersRepository(RetrofitBuilder.apiService, appPreference)
    }

    private val _username = MutableLiveData<Result<Boolean>>()
    val username: LiveData<Result<Boolean>> = _username

    private val _updateUserDetails = MutableLiveData<Event<Result<UserDetails>>>()
    val updateUserDetails: LiveData<Event<Result<UserDetails>>> = _updateUserDetails
    private val _saveUserDetails = MutableLiveData<Result<UserDetails>>()
    val saveUserDetails: LiveData<Result<UserDetails>> = _saveUserDetails
    private val _userDetails = MutableLiveData<Result<UserDetails>>()
    val userDetails: LiveData<Result<UserDetails>> = _userDetails

    fun saveUserDetails(userDetails: UserDetails) {
        viewModelScope.launch {
            _saveUserDetails.value = mUsersRepository.createUser(userDetails)
        }
    }

    fun updateUserDetails(userMiniDetails: UserMiniDetails) {
        viewModelScope.launch {
            _updateUserDetails.value = Event(mUsersRepository.updateUserDetails(userMiniDetails))
        }
    }

    fun isUsernamePresent(username: String) {
        viewModelScope.launch {
            _username.value = mUsersRepository.checkUsername(username)
        }
    }

    fun getUserData(uid: String) {
        viewModelScope.launch {
            _userDetails.value = mUsersRepository.getUser(uid)
        }
    }

}