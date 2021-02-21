package com.speakout.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakout.api.RetrofitBuilder
import com.speakout.common.Event
import com.speakout.common.Result
import com.speakout.notification.NotificationRepository
import com.speakout.notification.NotificationResponse
import com.speakout.notification.NotificationsItem
import com.speakout.utils.AppPreference
import kotlinx.coroutines.launch

class NotificationsViewModel(private val mRepository: NotificationRepository) : ViewModel() {

    companion object {
        const val MAX_SIZE = 20
    }

    val mNotifications = ArrayList<NotificationsItem>()
    private val _notifications = MutableLiveData<NotificationResponse>()
    val notifications: LiveData<NotificationResponse> = _notifications

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun getNotifications(pageNumber: Int) {
        viewModelScope.launch {
            val response = mRepository.getNotifications(
                pageNumber = pageNumber,
                pageSize = MAX_SIZE
            )
            if (response is Result.Success) {
                mNotifications.addAll(response.data.notifications)
                _notifications.value = response.data
            }
            if (response is Result.Error) {
                _error.value = Event(response.error.message!!)
            }
        }
    }

}