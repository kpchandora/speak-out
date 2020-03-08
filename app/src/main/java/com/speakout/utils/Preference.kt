package com.speakout.utils

import android.content.Context
import com.speakout.SpeakOutApp

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


    fun clearUserDetails() {
        editor?.apply {
            NameUtils.UserDetails?.apply {
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