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
import com.speakout.utils.AppPreference
import kotlinx.coroutines.launch

class NotificationsViewModel(private val mRepository: NotificationRepository) : ViewModel() {

    private val _notifications = MutableLiveData<Event<Result<List<NotificationResponse>>>>()
    val notifications: LiveData<Event<Result<List<NotificationResponse>>>> = _notifications

    fun getNotifications() {
        viewModelScope.launch {
            _notifications.value = Event(mRepository.getNotifications())
        }
    }

}