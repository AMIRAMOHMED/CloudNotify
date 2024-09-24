package com.example.cloudnotify.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cloudnotify.data.model.local.BookmarkLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertBookmarkLocation(bookmark: BookmarkLocation)

    @Query("DELETE FROM  bookmark_location WHERE id = :bookmarkId")
     fun deleteBookmarkLocationById(bookmarkId: Int)

    @Query("DELETE FROM  bookmark_location")
     fun deleteAllBookmarkLocations()

    @Query("SELECT * FROM bookmark_location")
    fun getAllBookmarkLocations(): Flow<List<BookmarkLocation>>

    @Query("SELECT COUNT(*) > 0 FROM bookmark_location WHERE id = :bookmarkId")
    fun isBookmarkLocationFavorite(bookmarkId: Int): Flow<Boolean>

}
