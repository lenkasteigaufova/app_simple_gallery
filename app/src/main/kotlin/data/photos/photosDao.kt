package app.data.photos

import androidx.room.*

@Dao
interface PhotosDao {
    @Query("SELECT * FROM photo")
    suspend fun getAll(): List<PhotoEntity>

    @Query("SELECT * FROM photo WHERE albumId=:albumId")
    suspend fun getPhotosByAlbumId(albumId : Int): List<PhotoEntity>

    @Insert
    suspend fun insertAll(photos: List<PhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(photo: PhotoEntity): Long

    @Delete
    suspend fun delete(photos: PhotoEntity)

    @Query("DELETE FROM photo")
    suspend fun deleteAll()

    @Query("DELETE FROM photo WHERE albumId=:albumId")
    suspend fun deleteByAlbumId(albumId : Int)
}