package app.data

import app.data.albums.AlbumItemEntity
import app.data.photos.NewPhoto
import app.data.photos.PhotoEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {

    @GET("/albums")
    suspend fun getAlbums(): List<AlbumItemEntity>

    @GET("/photos")
    suspend fun getAlbumPhotos(@Query("albumId") albumId: Int): List<PhotoEntity>

    @POST("/photos")
    suspend fun uploadPhoto(@Body newPhoto: NewPhoto)
}