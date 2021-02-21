package com.speakout.notification

import com.speakout.api.ApiService
import com.speakout.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class NotificationRepository(private val apiService: ApiService) {

    suspend fun getNotifications(pageNumber: Int, pageSize: Int): Result<NotificationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.getNotifications(pageNumber, pageSize)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e, null)
            }
        }

}