package com.speakout.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.speakout.utils.FirebaseUtils

class UserViewModel : ViewModel() {

    private val username = MutableLiveData<String>()
    private val updateUserDetails = MutableLiveData<Map<String, Any>>()
    private val saveUserDetails = MutableLiveData<UserDetails>()
    private val getUserData = MutableLiveData<String>()

    val usernameObserver: LiveData<FirebaseUtils.Data> = Transformations.switchMap(username) {
        AuthService.isUsernamePresentFirestore(key = it)
    }

    val updateDetailsObserver: LiveData<Boolean> = Transformations.switchMap(updateUserDetails) {
        AuthService.updateUserDataFirestore(it)
    }

    val saveUserDetailsObserver: LiveData<Boolean> = Transformations.switchMap(saveUserDetails) {
        AuthService.saveUserDataFirestore(it)
    }

    val getUserDataObserver: LiveData<UserDetails?> = Transformations.switchMap(getUserData) {
        AuthService.getUserDataFirestore(it)
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