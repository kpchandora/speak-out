package com.speakout.utils

import android.content.Context
import com.speakout.SpeakOutApp
import com.speakout.auth.UserDetails
import com.speakout.extensions.isNotNullOrEmpty

object AppPreference {

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
                putString(NameUtils.UserDetails.userId, userId)
            }

            if (email.isNotNullOrEmpty()) {
                putString(NameUtils.UserDetails.email, email!!)
            }

            if (name.isNotNullOrEmpty()) {
                putString(NameUtils.UserDetails.name, name!!)
            }

            if (username.isNotNullOrEmpty()) {
                putString(NameUtils.UserDetails.username, username!!)
            }

            if (photoUrl != null)
                putString(NameUtils.UserDetails.photoUrl, photoUrl)

            if (phoneNumber != null)
                putString(NameUtils.UserDetails.phoneNumber, phoneNumber)
        }
    }

    fun getPhoneNumber(): String {
        return getString(NameUtils.UserDetails.phoneNumber, "") ?: ""
    }

    fun getUserUniqueName(): String {
        return getString(NameUtils.UserDetails.username, "") ?: ""
    }

    fun getPhotoUrl(): String {
        return getString(NameUtils.UserDetails.photoUrl, "") ?: ""
    }

    fun updateDataChangeTimeStamp(timeInLong: Long) {
        putLong(NameUtils.UserDetails.lastUserDetailsUpdate, timeInLong)
    }

    fun getLastUpdatedTime(): Long {
        return getLong(NameUtils.UserDetails.lastUserDetailsUpdate)
    }

    fun getUserDisplayName(): String {
        return getString(NameUtils.UserDetails.name, "") ?: ""
    }

    fun getUserId(): String {
        return getString(NameUtils.UserDetails.userId, "") ?: ""
    }

    fun setLoggedIn() {
        putBoolean(NameUtils.UserDetails.isLoggedIn, true)
    }

    fun setUsernameProcessComplete() {
        putBoolean(NameUtils.UserDetails.usernameProcess, true)
    }

    fun isUsernameProcessComplete(): Boolean {
        return getBoolean(NameUtils.UserDetails.usernameProcess)
    }

    fun isLoggedIn(): Boolean {
        return getBoolean(NameUtils.UserDetails.isLoggedIn)
    }

    fun clearUserDetails() {
        editor?.apply {
            NameUtils.UserDetails?.apply {
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