package com.speakoutall.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakoutall.common.Event
import com.speakoutall.common.Result
import com.speakoutall.notification.NotificationRepository
import com.speakoutall.notification.NotificationResponse
import com.speakoutall.notification.NotificationsItem
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

    fun getNotifications(key: Long) {
        viewModelScope.launch {
            val response = mRepository.getNotifications(
                key = key,
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

    fun updateActions() {
        viewModelScope.launch {
            mRepository.updateActions()
        }
    }

}