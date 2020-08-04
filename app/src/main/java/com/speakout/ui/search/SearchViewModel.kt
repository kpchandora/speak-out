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
import com.speakout.utils.AppPreference
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val mUsersRepository by lazy {
        UsersRepository(RetrofitBuilder.apiService, AppPreference)
    }

    private val _searchUsers = MutableLiveData<Event<Result<List<UserMiniDetails>>>>()
    val searchUsers: LiveData<Event<Result<List<UserMiniDetails>>>> = _searchUsers


    fun searchUsers(query: String) {
        viewModelScope.launch {
            _searchUsers.value = Event(mUsersRepository.searchUsers(query))
        }
    }
}