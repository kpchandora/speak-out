package com.speakout.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.speakout.posts.create.PostData
import com.speakout.ui.home.HomeService
import com.speakout.utils.AppPreference

class UsersListViewModel : ViewModel() {

    private val _usersList = MutableLiveData<String>()
    val usersList: LiveData<List<PostData>> = _usersList.switchMap {
        HomeService.getPosts(AppPreference.getUserId())
    }

    fun getPosts(id: String) {
        _usersList.value = ""
    }

}
