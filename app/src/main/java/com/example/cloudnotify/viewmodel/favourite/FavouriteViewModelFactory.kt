package com.example.cloudnotify.viewmodel.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cloudnotify.data.repo.BookmarkRepository

class FavouriteViewModelFactory(
    private val bookmarkRepository: BookmarkRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavouriteViewModel::class.java)) {
            return FavouriteViewModel(bookmarkRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
