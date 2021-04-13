package com.speakoutall.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakoutall.auth.UsersItem
import com.speakoutall.common.Event
import com.speakoutall.users.UsersRepository
import com.speakoutall.common.Result
import kotlinx.coroutines.launch

class SearchViewModel(private val mUsersRepository: UsersRepository) : ViewModel() {

    private val _searchUsers = MutableLiveData<Event<Result<List<UsersItem>>>>()
    val searchUsers: LiveData<Event<Result<List<UsersItem>>>> = _searchUsers


    fun searchUsers(query: String) {
        viewModelScope.launch {
            _searchUsers.value = Event(mUsersRepository.searchUsers(username = query))
        }
    }
}