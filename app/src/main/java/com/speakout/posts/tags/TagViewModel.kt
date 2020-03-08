package com.speakout.posts.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class TagViewModel : ViewModel() {


    private val _tags = MutableLiveData<String>()
    val tags: LiveData<List<Tag>> = Transformations.switchMap(_tags) {
        TagsService.getTags(it)
    }

    fun searchTags(query: String) {
        _tags.value = query
    }

}