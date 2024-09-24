package com.example.cloudnotify.data.repo

import com.example.cloudnotify.data.local.db.BookmarkLocationDao
import com.example.cloudnotify.data.model.local.BookmarkLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class BookmarkRepository(private val bookmarkLocationDao: BookmarkLocationDao) {

    // Get all bookmarks
    fun getAllBookmarkLocations(): Flow<List<BookmarkLocation>> {
        return bookmarkLocationDao.getAllBookmarkLocations()
    }

    // Insert a new bookmark
    suspend fun insertBookmark(bookmark: BookmarkLocation) {
        bookmarkLocationDao.insertBookmarkLocation(bookmark)
    }

    // Delete a bookmark by ID
    suspend fun deleteBookmarkById(id: Int) {
        bookmarkLocationDao.deleteBookmarkLocationById(id)
    }

    // Delete all bookmarks
    suspend fun deleteAllBookmarks() {
        bookmarkLocationDao.deleteAllBookmarkLocations()
    }

    // Check if a bookmark is favorite
    suspend fun isBookmarkFavorite(id: Int): Boolean {
        return bookmarkLocationDao.isBookmarkLocationFavorite(id).firstOrNull() ?: false
    }
}