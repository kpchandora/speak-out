package com.speakout.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.speakout.auth.AuthService
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.people.FollowUnfollowService
import com.speakout.posts.create.PostData
import com.speakout.ui.UserLiveData

class ProfileViewModel : ViewModel() {

    val profileObserver: LiveData<UserDetails?> = UserLiveData()

    private val _userDetails = MutableLiveData<String>()

    val userDetails: LiveData<UserDetails?> = _userDetails.switchMap {
        AuthService.getUserData(it)
    }

    private val _followUser = MutableLiveData<UserMiniDetails>()
    val followUser: LiveData<Boolean> = _followUser.switchMap {
        FollowUnfollowService.unfollow(it)
    }

    fun getUser(uid: String) {
        _userDetails.value = uid
    }

    fun followUser(userMiniDetails: UserMiniDetails) {
        _followUser.value = userMiniDetails
    }

}