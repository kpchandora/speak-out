package com.speakout.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _navigateToProfileFragment = MutableLiveData<String>()
    val navigateToProfileFragment: LiveData<String>
        get() = _navigateToProfileFragment


    fun navigateToProfileFragment(userId: String) {
        _navigateToProfileFragment.value = userId
    }
}