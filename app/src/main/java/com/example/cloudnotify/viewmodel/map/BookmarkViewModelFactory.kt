package com.example.cloudnotify.viewmodel.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cloudnotify.data.repo.BookmarkRepository

class BookmarkViewModelFactory(
    private val bookmarkRepository: BookmarkRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookmarkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookmarkViewModel(bookmarkRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}