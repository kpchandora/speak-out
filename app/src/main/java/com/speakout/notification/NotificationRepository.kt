package com.speakout.notification

import com.speakout.api.ApiService
import com.speakout.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class NotificationRepository(private val apiService: ApiService) {

    suspend fun getNotifications(key: Long, pageSize: Int): Result<NotificationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.getNotifications(key, pageSize)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Timber.e(e)
                Result.Error(e, null)
            }
        }

    suspend fun updateActions(): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.updateActions()
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(true)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Result.Error(e, null)
            }
        }

}