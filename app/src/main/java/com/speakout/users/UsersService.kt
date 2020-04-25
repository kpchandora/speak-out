package com.speakout.users

import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.speakout.auth.UserDetails
import com.speakout.auth.UserMiniDetails
import com.speakout.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.speakout.common.Result.Success
import com.speakout.utils.FirebaseUtils.FirestoreUtils.getUsersRef
import com.speakout.utils.FirebaseUtils.getFirebaseFunction
import com.speakout.utils.StringJava
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.Exception

object UsersService {

    suspend fun getLikesList(postId: String): Result<List<UserMiniDetails>> =
        withContext(Dispatchers.IO) {
            try {
                val data = mapOf("postId" to postId)
                val task = getFirebaseFunction("getLikesDetails").call(data).await()
                val json = Gson().toJson(task.data)
                val list =
                    Gson().fromJson(json, Array<UserMiniDetails>::class.java)
                        .asList()
                Success(list)
            } catch (e: Exception) {
                Result.Error(e, null)
            }
        }

    suspend fun getFollowers(userId: String): Result<List<UserMiniDetails>> =
        withContext(Dispatchers.IO) {
            try {
                val data = mapOf("userId" to userId)
                val task = getFirebaseFunction("getFollowers").call(data).await()
                val json = Gson().toJson(task.data)
                val list =
                    Gson().fromJson(json, Array<UserMiniDetails>::class.java)
                        .asList()
                Success(list)
            } catch (e: Exception) {
                Timber.d("getFollowers error: $e")
                Result.Error(e, null)
            }
        }

    suspend fun getFollowings(userId: String): Result<List<UserMiniDetails>> =
        withContext(Dispatchers.IO) {
            try {
                val data = mapOf("userId" to userId)
                val task = getFirebaseFunction("getFollowings").call(data).await()
                Timber.d("getFollowings: ${task.data}")
                val json = Gson().toJson(task.data)
                val list =
                    Gson().fromJson(json, Array<UserMiniDetails>::class.java)
                        .asList()
                Success(list)
            } catch (e: Exception) {
                Result.Error(e, null)
            }
        }

    suspend fun searchUsers(usernameQuery: String): List<UserMiniDetails> =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Search Query: $usernameQuery")
                val nextChar = StringJava.next(usernameQuery)
                var usersRef = getUsersRef().whereGreaterThanOrEqualTo("username", usernameQuery)
                if (usernameQuery.isNotEmpty()) {
                    usersRef = usersRef.whereLessThan("username", nextChar)
                }

                val querySnapshot = usersRef.limit(20).get().await()
                if (querySnapshot.isEmpty) {
                    Timber.d("Search users list empty")
                    emptyList<UserMiniDetails>()
                } else {
                    val usersList = mutableListOf<UserMiniDetails>()
                    querySnapshot.forEach { singleUserSnapshot ->
                        try {
                            val user = singleUserSnapshot.toObject(UserDetails::class.java)
                            usersList.add(
                                UserMiniDetails(
                                    userId = user.userId, username = user.username,
                                    name = user.name, photoUrl = user.photoUrl
                                )
                            )
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    }
                    Timber.d("Search Users List: $usersList")
                    usersList
                }
            } catch (e: Exception) {
                Timber.e(e)
                emptyList<UserMiniDetails>()
            }
        }

}