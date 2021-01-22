package com.speakout.ui.search

import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.*

internal class DebouncingQueryTextListener(
    private val onDebouncingQueryTextChange: (String?) -> Unit,
    private val runBefore: (String?) -> Unit = {}
) : SearchView.OnQueryTextListener {
    var debouncePeriod: Long = 1000

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    private var searchJob: Job? = null

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchJob?.cancel()
        runBefore(newText)
        searchJob = coroutineScope.launch {
            newText?.let {
                delay(debouncePeriod)
                onDebouncingQueryTextChange(newText)
            }
        }
        return true
    }
}