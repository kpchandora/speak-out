package com.speakout.posts.tags

import androidx.lifecycle.*
import kotlinx.coroutines.*
import timber.log.Timber

class TagViewModel : ViewModel() {


    private var searchJob: Job? = null

    private val _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>> = _tags

    private val _addTag = MutableLiveData<Tag>()
    val addTag: LiveData<Tag?> = Transformations.switchMap(_addTag) {
        TagsService.addTag(tag = it)
    }

    fun addTag(tag: Tag) {
        _addTag.value = tag
    }

    fun searchTags(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            Timber.d("launch")
            _tags.value = TagsService.getTags(query)
        }
    }

}