package com.example.cloudnotify.viewmodel.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.data.repo.BookmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavouriteViewModel(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    // StateFlow to hold the bookmark list
    private val _bookmarkList = MutableStateFlow<List<BookmarkLocation>>(emptyList())
    val bookmarkList: StateFlow<List<BookmarkLocation>> get() = _bookmarkList

    // Initialize and collect bookmark locations from repository
    init {
        getBookmarks()
    }

    // Function to fetch all bookmark locations
    private fun getBookmarks() {
        viewModelScope.launch {
            bookmarkRepository.getAllBookmarkLocations().collect { bookmarkList ->
                _bookmarkList.value = bookmarkList
            }
        }
    }


    // Function to delete a bookmark
    fun deleteBookmark(bookmarkLocation: BookmarkLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkRepository.deleteBookmarkById(bookmarkLocation.id)
        }
    }
}
