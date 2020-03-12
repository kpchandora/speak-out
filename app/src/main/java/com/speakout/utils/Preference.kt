package com.speakout.utils

import android.content.Context
import com.speakout.SpeakOutApp
import com.speakout.auth.UserDetails
import com.speakout.extensions.isNotNullOrEmpty

class Preference {

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

            if (username.isNotNullOrEmpty()) {
                putString(NameUtils.UserDetails.username, username!!)
            }

            if (photoUrl.isNotNullOrEmpty()) {
                putString(NameUtils.UserDetails.photoUrl, photoUrl!!)
            }

            if (phoneNumber.isNotNullOrEmpty()) {
                putString(NameUtils.UserDetails.phoneNumber, phoneNumber!!)
            }
        }
    }

    fun getUserUniqueName(): String? {
        return getString(NameUtils.UserDetails.username, "")
    }

    fun getPhotoUrl(): String? {
        return getString(NameUtils.UserDetails.photoUrl, "")
    }

    fun getUserDisplayName(): String? {
        return getString(NameUtils.UserDetails.name, "")
    }

    fun getUserId(): String? {
        return getString(NameUtils.UserDetails.userId, "")
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
            }
        }?.apply()
    }

}