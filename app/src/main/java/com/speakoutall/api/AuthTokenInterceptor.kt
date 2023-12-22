package com.speakoutall.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.speakoutall.BuildConfig
import com.speakoutall.utils.AppPreference
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * Created by Kalpesh on 08/08/20.
 */
class AuthTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            val task = FirebaseAuth.getInstance().currentUser?.getIdToken(true)
                ?: throw Exception("User not logged in")

            val pair = shouldFetchToken()
            if (!pair.first) {
                Timber.d("End")
                val modifiedRequest = request.newBuilder()
                    .addHeader("Authorization", "Bearer ${pair.second}")
                    .addHeader("version_name", BuildConfig.VERSION_NAME)
                    .addHeader("version_code", BuildConfig.VERSION_CODE.toString())
                    .build()
                return chain.proceed(modifiedRequest)
            } else {
                val result = Tasks.await(task)
                result?.token?.let { token ->
                    AppPreference.updateFirebaseToken(token)
                    val modifiedRequest = request.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("version_name", BuildConfig.VERSION_NAME)
                        .addHeader("version_code", BuildConfig.VERSION_CODE.toString())
                        .build()
                    return chain.proceed(modifiedRequest)
                } ?: kotlin.run {
                    throw Exception("idToken is null")
                }
            }
        } catch (e: Exception) {
            throw IOException(e.message)
        }
    }

    private fun shouldFetchToken(): Pair<Boolean, String> {
        val pair = AppPreference.getFirebaseTokenAndTime()
        val timeDiff = System.currentTimeMillis() - pair.second
        // Checks if the token is expired or not. The below conditions checks for 30 minutes
        // The real token expiry time is 60 minutes
        if (timeDiff > (1000 * 60 * 30)) {
            return Pair(true, "")
        }
        return Pair(false, pair.first)
    }

}