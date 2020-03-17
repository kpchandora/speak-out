package com.speakout.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.speakout.auth.AuthService
import com.speakout.auth.UserDetails
import com.speakout.posts.create.PostData
import com.speakout.ui.UserLiveData

class ProfileViewModel : ViewModel() {

    val profileObserver: LiveData<UserDetails?> = UserLiveData()

    private val _userDetails = MutableLiveData<String>()

    val userDetails: LiveData<UserDetails?> = _userDetails.switchMap {
        AuthService.getUserData(it)
    }

    fun getUser(uid: String) {
        _userDetails.value = uid
    }

}