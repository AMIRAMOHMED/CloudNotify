package com.example.cloudnotify.viewmodel.map



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudnotify.data.model.local.BookmarkLocation
import com.example.cloudnotify.data.repo.BookmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    // Check if bookmark is favorite using Flow
    fun isBookmarkFavorite(bookmarkId: Int): Flow<Boolean> {
        return flow {
            emit(bookmarkRepository.isBookmarkFavorite(bookmarkId))
        }.flowOn(Dispatchers.IO) // Runs on IO thread
    }

    // Toggle bookmark state using Flow
    fun toggleBookmark(currentWeatherItem: BookmarkLocation, isFavorite: Boolean): Flow<Boolean> {
        return flow {
            if (isFavorite) {
                bookmarkRepository.deleteBookmarkById(currentWeatherItem.id)
            } else {
                bookmarkRepository.insertBookmark(currentWeatherItem)
            }
            emit(!isFavorite) // Emit new favorite state
        }.flowOn(Dispatchers.IO)
    }
}
