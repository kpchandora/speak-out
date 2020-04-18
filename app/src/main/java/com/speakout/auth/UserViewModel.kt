package com.speakout.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.speakout.common.Event
import com.speakout.utils.FirebaseUtils

class UserViewModel : ViewModel() {

    private val username = MutableLiveData<String>()
    private val updateUserDetails = MutableLiveData<Map<String, Any>>()
    private val saveUserDetails = MutableLiveData<UserDetails>()
    private val getUserData = MutableLiveData<String>()

    val usernameObserver: LiveData<FirebaseUtils.Data> = Transformations.switchMap(username) {
        AuthService.isUsernamePresent(key = it)
    }

    val updateDetailsObserver: LiveData<Event<Boolean>> = Transformations.switchMap(updateUserDetails) {
        AuthService.updateUserData(it)
    }

    val saveUserDetailsObserver: LiveData<Boolean> = Transformations.switchMap(saveUserDetails) {
        AuthService.saveUserData(it)
    }

    val getUserDataObserver: LiveData<UserDetails?> = Transformations.switchMap(getUserData) {
        AuthService.getUserData(it)
    }

    fun saveUserDetails(userDetails: UserDetails) {
        saveUserDetails.value = userDetails
    }

    fun updateUserDetails(detailsMap: Map<String, Any>) {
        updateUserDetails.value = detailsMap
    }

    fun isUsernamePresent(key: String) {
        username.value = key
    }

    fun getUserData(uid: String) {
        getUserData.value = uid
    }

}