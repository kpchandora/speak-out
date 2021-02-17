package com.speakout.users

import androidx.lifecycle.*
import com.speakout.api.RetrofitBuilder
import com.speakout.auth.UserMiniDetails
import com.speakout.common.Result
import com.speakout.utils.AppPreference
import kotlinx.coroutines.launch

class UsersListViewModel(private val mUsersRepository: UsersRepository) : ViewModel() {

    private val _likesList = MutableLiveData<Result<List<UserMiniDetails>>>()
    val likesList: LiveData<Result<List<UserMiniDetails>>> = _likesList

    private val _followersList = MutableLiveData<Result<List<UserMiniDetails>>>()
    val followersList: LiveData<Result<List<UserMiniDetails>>> = _followersList

    private val _followingsList = MutableLiveData<Result<List<UserMiniDetails>>>()
    val followingsList: LiveData<Result<List<UserMiniDetails>>> = _followingsList

    fun getLikesList(postId: String) {
        viewModelScope.launch {
            _likesList.value = mUsersRepository.getUsersList(
                userId = AppPreference.getUserId(),
                postId = postId,
                actionType = ActionType.Likes
            )
        }
    }

    fun getFollowingsList(userId: String) {
        viewModelScope.launch {
            _followingsList.value = mUsersRepository.getUsersList(
                userId = userId, actionType = ActionType.Followings
            )
        }
    }

    fun getFollowersList(userId: String) {
        viewModelScope.launch {
            _followersList.value = mUsersRepository.getUsersList(
                userId = userId, actionType = ActionType.Followers
            )
        }
    }

}
