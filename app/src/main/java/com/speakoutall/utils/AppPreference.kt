package com.speakoutall.utils

import android.content.Context
import com.speakoutall.SpeakOutApp
import com.speakoutall.auth.UserDetails
import com.speakoutall.extensions.isNotNullOrEmpty

object AppPreference {

    private const val FIREBASE_TOKEN = "firebase_token"
    private const val FIREBASE_TOKEN_TIME = "firebase_token_time"

    private val sharedPreferences by lazy {
        SpeakOutApp.appContext?.getSharedPreferences(
            "speak_out_pref", Context.MODE_PRIVATE
        )
    }

    private val editor by lazy {
        sharedPreferences?.edit()
    }

    fun putInt(key: String, value: Int) {
        editor?.putInt(key, value)?.apply()
    }

    fun getInt(key: String, defaultValue: Int = -1): Int {
        return sharedPreferences?.getInt(key, defaultValue) ?: -1
    }

    fun putLong(key: String, value: Long) {
        editor?.putLong(key, value)?.apply()
    }

    fun getLong(key: String, defaultValue: Long = -1): Long {
        return sharedPreferences?.getLong(key, defaultValue) ?: -1
    }

    fun putString(key: String, value: String) {
        editor?.putString(key, value)?.apply()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences?.getString(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        editor?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences?.getBoolean(key, defaultValue) ?: false
    }

    fun remove(key: String) {
        editor?.remove(key)?.apply()
    }

    fun saveUserDetails(userDetails: UserDetails) {
        userDetails.apply {
            if (userId.isNotEmpty()) {
                putString(Constants.UserDetails.userId, userId)
            }

            if (email.isNotNullOrEmpty()) {
                putString(Constants.UserDetails.email, email!!)
            }

            if (name.isNotNullOrEmpty()) {
                putString(Constants.UserDetails.name, name!!)
            }

            if (username.isNotNullOrEmpty()) {
                putString(Constants.UserDetails.username, username!!)
            }

            if (photoUrl != null)
                putString(Constants.UserDetails.photoUrl, photoUrl)

            if (phoneNumber != null)
                putString(Constants.UserDetails.phoneNumber, phoneNumber)
        }
    }

    fun getPhoneNumber(): String {
        return getString(Constants.UserDetails.phoneNumber, "") ?: ""
    }

    fun getUserUniqueName(): String {
        return getString(Constants.UserDetails.username, "") ?: ""
    }

    fun getPhotoUrl(): String {
        return getString(Constants.UserDetails.photoUrl, "") ?: ""
    }

    fun updateDataChangeTimeStamp(timeInLong: Long) {
        putLong(Constants.UserDetails.lastUserDetailsUpdate, timeInLong)
    }

    fun getLastUpdatedTime(): Long {
        return getLong(Constants.UserDetails.lastUserDetailsUpdate)
    }

    fun getUserDisplayName(): String {
        return getString(Constants.UserDetails.name, "") ?: ""
    }

    fun getUserId(): String {
        return getString(Constants.UserDetails.userId, "") ?: ""
    }

    fun setLoggedIn() {
        putBoolean(Constants.UserDetails.isLoggedIn, true)
    }

    fun setUsernameProcessComplete() {
        putBoolean(Constants.UserDetails.usernameProcess, true)
    }

    fun isUsernameProcessComplete(): Boolean {
        return getBoolean(Constants.UserDetails.usernameProcess)
    }

    fun isLoggedIn(): Boolean {
        return getBoolean(Constants.UserDetails.isLoggedIn)
    }

    fun updateFirebaseToken(token: String) {
        putString(FIREBASE_TOKEN, token)
        putLong(FIREBASE_TOKEN_TIME, System.currentTimeMillis())
    }

    fun getFirebaseTokenAndTime(): Pair<String, Long> {
        return Pair(getString(FIREBASE_TOKEN) ?: "", getLong(FIREBASE_TOKEN_TIME))
    }

    fun clearFirebaseToken() {
        remove(FIREBASE_TOKEN_TIME)
        remove(FIREBASE_TOKEN)
    }

    fun clearUserDetails() {
        editor?.apply {
            Constants.UserDetails?.apply {
                remove(userId)
                remove(username)
                remove(email)
                remove(name)
                remove(phoneNumber)
                remove(lastLogin)
                remove(isLoggedIn)
                remove(photoUrl)
                remove(usernameProcess)
            }
        }?.apply()
    }

}