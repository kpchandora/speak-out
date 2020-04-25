package com.speakout.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakout.auth.UserMiniDetails
import com.speakout.common.Event
import com.speakout.users.UsersService
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _searchUsers = MutableLiveData<Event<List<UserMiniDetails>>>()
    val searchUsers: LiveData<Event<List<UserMiniDetails>>> = _searchUsers


    fun searchUsers(query: String) {
        viewModelScope.launch {
            _searchUsers.value = Event(UsersService.searchUsers(query))
        }
    }
}