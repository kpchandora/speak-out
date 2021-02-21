package com.speakout.users

import androidx.lifecycle.*
import com.speakout.auth.UserResponse
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

    private val _likesList = MutableLiveData<Result<UserResponse>>()
    val likesList: LiveData<Result<UserResponse>> = _likesList

    private val _followersList = MutableLiveData<Result<UserResponse>>()
    val followersList: LiveData<Result<UserResponse>> = _followersList

    private val _followingsList = MutableLiveData<Result<UserResponse>>()
    val followingsList: LiveData<Result<UserResponse>> = _followingsList

    fun getLikesList(postId: String, pageNumber: Int) {
        viewModelScope.launch {
            _likesList.value = mUsersRepository.getUsersList(
                userId = appPreference.getUserId(),
                postId = postId,
                actionType = ActionType.Likes,
                pageNumber = pageNumber,
                pageSize = MAX_PAGE_SIZE
            )
        }
    }

    fun getFollowingsList(userId: String, pageNumber: Int) {
        viewModelScope.launch {
            _followingsList.value = mUsersRepository.getUsersList(
                userId = userId, actionType = ActionType.Followings,
                pageNumber = pageNumber,
                pageSize = MAX_PAGE_SIZE
            )
        }
    }

    fun getFollowersList(userId: String, pageNumber: Int) {
        viewModelScope.launch {
            _followersList.value = mUsersRepository.getUsersList(
                userId = userId, actionType = ActionType.Followers,
                pageNumber = pageNumber,
                pageSize = MAX_PAGE_SIZE
            )
        }
    }

}
