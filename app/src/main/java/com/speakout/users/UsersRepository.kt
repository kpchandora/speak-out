package com.speakout.users

import com.google.gson.JsonObject
import com.speakout.api.ApiService
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.common.Result
import com.speakout.utils.AppPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Created by Kalpesh on 02/08/20.
 */
public class UsersRepository(
    private val apiService: ApiService,
    private val appPreference: AppPreference
) {
    suspend fun createUser(userDetails: UserDetails): Result<UserDetails> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.createUser(userDetails)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Failed to create user"), null)
            } catch (e: Exception) {
                Result.Error(e, null)
            }
        }

    suspend fun getUser(userId: String): Result<UserDetails> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.getUser(appPreference.getUserId(), userId)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Result.Error(e, null)
            }
        }

    suspend fun checkUsername(username: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.checkUserName(username)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!.get("isPresent").asBoolean)
                }
                Result.Error(Exception("Something went wrong"), false)
            } catch (e: Exception) {
                Result.Error(e, false)
            }
        }

    suspend fun updateUserDetails(userMiniDetails: UserMiniDetails): Result<UserDetails> =
        withContext(Dispatchers.IO) {
            try {
                val result = apiService.updateUserDetails(userMiniDetails)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Something went wrong"), null)
            } catch (e: Exception) {
                Result.Error(e, null)
            }
        }

    suspend fun followUser(userId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val obj = JsonObject()
                obj.addProperty("selfUserId", appPreference.getUserId())
                obj.addProperty("userId", userId)
                val result = apiService.followUser(obj)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(userId)
                }
                Result.Error(Exception("Failed to follow user"), userId)
            } catch (e: Exception) {
                Result.Error(e, userId)
            }
        }

    suspend fun unFollowUser(userId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val obj = JsonObject()
                obj.addProperty("selfUserId", appPreference.getUserId())
                obj.addProperty("userId", userId)
                val result = apiService.unFollowUser(obj)
                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(userId)
                }
                Result.Error(Exception("Failed to unfollow user"), userId)
            } catch (e: Exception) {
                Result.Error(e, userId)
            }
        }

    suspend fun getUsersList(
        userId: String,
        postId: String = "",
        actionType: ActionType
    ): Result<List<UserMiniDetails>> =
        withContext(Dispatchers.IO) {
            try {
                val result: Response<List<UserMiniDetails>>
                when (actionType) {
                    ActionType.Likes -> {
                        result = apiService.getFollowers(
                            selfUserId = appPreference.getUserId(), userId = userId
                        )
                    }
                    ActionType.Followers -> {
                        result = apiService.getFollowers(
                            selfUserId = appPreference.getUserId(), userId = userId
                        )
                    }
                    ActionType.Followings -> {
                        result = apiService.getFollowings(
                            selfUserId = appPreference.getUserId(), userId = userId
                        )
                    }
                }

                if (result.isSuccessful && result.body() != null) {
                    return@withContext Result.Success(result.body()!!)
                }
                Result.Error(Exception("Failed to get data"), emptyList<UserMiniDetails>())
            } catch (e: Exception) {
                Result.Error(e, emptyList<UserMiniDetails>())
            }
        }



}