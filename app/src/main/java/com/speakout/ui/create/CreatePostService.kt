package com.speakout.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object CreatePostService {

    fun createPost(postData: CreatePostData): LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()

        return data
    }

}