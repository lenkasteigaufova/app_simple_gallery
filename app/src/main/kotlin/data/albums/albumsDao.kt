package app.data.albums

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlbumsDao {
    @Query("SELECT * FROM album_item")
    suspend fun getAll(): List<AlbumItemEntity>

    @Insert
    suspend fun insertAll(albums: List<AlbumItemEntity>)

    @Delete
    suspend fun delete(albums: AlbumItemEntity)

    @Query("DELETE FROM album_item")
    suspend fun deleteAll()
}