package app.data.albums

import androidx.room.*

@Entity (tableName = "album_item")
data class AlbumItemEntity(
    @PrimaryKey
    val id              : Int,
    val userId          : Int,
    val title           : String,
    val fetchTimestamp  : Long
)