package com.speakoutall.users

import androidx.lifecycle.*
import com.speakoutall.auth.UserResponse
import com.speakoutall.auth.UsersItem
import com.speakoutall.common.Event
import com.speakoutall.common.Result
import com.speakoutall.utils.AppPreference
import kotlinx.coroutines.launch

class UsersListViewModel(
    private val appPreference: AppPreference,
    private val mUsersRepository: UsersRepository
) : ViewModel() {

    companion object {
        const val MAX_PAGE_SIZE = 20
    }

    val mUsersList = ArrayList<UsersItem>()
    private val _usersList = MutableLiveData<UserResponse>()
    val usersList: LiveData<UserResponse> = _usersList

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun getLikesList(postId: String, key: Long) {
        viewModelScope.launch {
            val response = mUsersRepository.getUsersList(
                userId = appPreference.getUserId(),
                postId = postId,
                actionType = ActionType.Likes,
                key = key,
                pageSize = MAX_PAGE_SIZE
            )
            if (response is Result.Success) {
                mUsersList.addAll(response.data.users)
                _usersList.value = response.data
            }
            if (response is Result.Error) {
                _error.value = Event(response.error.message!!)
            }
        }
    }

    fun getFollowingsList(userId: String, key: Long) {
        viewModelScope.launch {
            val response = mUsersRepository.getUsersList(
                userId = userId,
                actionType = ActionType.Followings,
                key = key,
                pageSize = MAX_PAGE_SIZE
            )
            if (response is Result.Success) {
                mUsersList.addAll(response.data.users)
                _usersList.value = response.data
            }
            if (response is Result.Error) {
                _error.value = Event(response.error.message!!)
            }
        }
    }

    fun getFollowersList(userId: String, key: Long) {
        viewModelScope.launch {
            val response = mUsersRepository.getUsersList(
                userId = userId, actionType = ActionType.Followers,
                key = key,
                pageSize = MAX_PAGE_SIZE
            )
            if (response is Result.Success) {
                mUsersList.addAll(response.data.users)
                _usersList.value = response.data
            }
            if (response is Result.Error) {
                _error.value = Event(response.error.message!!)
            }
        }
    }

}
