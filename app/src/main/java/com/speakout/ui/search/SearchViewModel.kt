package com.speakout.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakout.api.RetrofitBuilder
import com.speakout.auth.UserMiniDetails
import com.speakout.common.Event
import com.speakout.users.UsersRepository
import com.speakout.common.Result
import com.speakout.users.UsersListViewModel
import com.speakout.utils.AppPreference
import kotlinx.coroutines.launch

class SearchViewModel(private val mUsersRepository: UsersRepository) : ViewModel() {

    private val _searchUsers = MutableLiveData<Event<Result<List<UserMiniDetails>>>>()
    val searchUsers: LiveData<Event<Result<List<UserMiniDetails>>>> = _searchUsers


    fun searchUsers(query: String) {
        viewModelScope.launch {
            _searchUsers.value = Event(mUsersRepository.searchUsers(username = query))
        }
    }
}