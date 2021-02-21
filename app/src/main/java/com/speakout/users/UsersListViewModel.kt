package com.speakout.users

import androidx.lifecycle.*
import com.speakout.auth.UserResponse
import com.speakout.auth.UsersItem
import com.speakout.common.Event
import com.speakout.common.Result
import com.speakout.utils.AppPreference
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

    fun getLikesList(postId: String, pageNumber: Int) {
        viewModelScope.launch {
            val response = mUsersRepository.getUsersList(
                userId = appPreference.getUserId(),
                postId = postId,
                actionType = ActionType.Likes,
                pageNumber = pageNumber,
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

    fun getFollowingsList(userId: String, pageNumber: Int) {
        viewModelScope.launch {
            val response = mUsersRepository.getUsersList(
                userId = userId,
                actionType = ActionType.Followings,
                pageNumber = pageNumber,
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

    fun getFollowersList(userId: String, pageNumber: Int) {
        viewModelScope.launch {
            val response = mUsersRepository.getUsersList(
                userId = userId, actionType = ActionType.Followers,
                pageNumber = pageNumber,
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
