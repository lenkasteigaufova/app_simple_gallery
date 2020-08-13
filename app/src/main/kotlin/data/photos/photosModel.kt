package app.data.photos

import androidx.room.*

data class NewPhoto(
    val title: String,
    val albumId: Int
)

@Entity(tableName = "photo")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id              : Int,
    val albumId         : Int,
    val title           : String,
    val url             : String,
    val thumbnailUrl    : String,
    val fetchTimestamp  : Long
)