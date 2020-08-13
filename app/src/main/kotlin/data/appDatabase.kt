package app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.data.albums.AlbumsDao
import app.data.albums.AlbumItemEntity
import app.data.photos.PhotosDao
import app.data.photos.PhotoEntity

@Database(entities = [PhotoEntity::class, AlbumItemEntity::class], version = 6)
abstract class AppDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotosDao
    abstract fun albumDao(): AlbumsDao

    companion object {
        private const val DATABASE_NAME = "photo_album_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .build()
        }

    }

}