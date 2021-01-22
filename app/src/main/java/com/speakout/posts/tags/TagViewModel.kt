package com.speakout.posts.tags

import androidx.lifecycle.*
import com.speakout.api.RetrofitBuilder
import com.speakout.posts.TagsRepository
import kotlinx.coroutines.*
import timber.log.Timber

class TagViewModel : ViewModel() {


    private var searchJob: Job? = null
    private val mTagsRepository: TagsRepository by lazy {
        TagsRepository(RetrofitBuilder.apiService)
    }

    private val _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>> = _tags

    private val _addTag = MutableLiveData<Tag?>()
    val addTag: LiveData<Tag?> = _addTag

    fun addTag(tag: Tag) {
        viewModelScope.launch {
            _addTag.value = mTagsRepository.createTag(tag)
        }
    }

    fun searchTags(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            Timber.d("launch")
            _tags.value = mTagsRepository.getTags(query)
        }
    }

}