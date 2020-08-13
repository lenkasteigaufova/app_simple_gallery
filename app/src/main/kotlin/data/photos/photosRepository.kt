package app.data.photos

import app.data.Api
import java.util.concurrent.TimeUnit

private val PHOTOS_CACHE_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(5)


interface PhotosRepository {

    suspend fun getPhotos(albumId: Int) : List<PhotoEntity>
    suspend fun uploadPhoto(photoTitle: String, albumId: Int)
}

class PhotosRepositoryIml(private val photosApi: Api, val photoDao: PhotosDao) : PhotosRepository {

    override suspend fun getPhotos(albumId: Int) : List<PhotoEntity> {

        var photos = photoDao.getPhotosByAlbumId(albumId)

        if (photos.isNotEmpty()) {
            if ((photos.first().fetchTimestamp + PHOTOS_CACHE_EXPIRATION_TIME) < System.currentTimeMillis()) {
                photos = fetchPhotosFromAlbum(albumId)
                photoDao.deleteByAlbumId(albumId)
                photoDao.insertAll(photos)
            }
        } else {
            photos = fetchPhotosFromAlbum(albumId)
            photoDao.insertAll(photos)
        }
        return photos
    }

    private suspend fun fetchPhotosFromAlbum(albumId: Int) = photosApi.getAlbumPhotos(albumId).map {
        it.copy(albumId = albumId, fetchTimestamp = System.currentTimeMillis())
    }

    override suspend fun uploadPhoto(photoTitle: String, albumId: Int) {
        photosApi.uploadPhoto(NewPhoto(photoTitle, albumId))
        photoDao.insert(PhotoEntity(id = 0, title = photoTitle, url = "https://via.placeholder.com/600/b4412f", albumId = albumId, thumbnailUrl = "https://via.placeholder.com/600/b4412f", fetchTimestamp = System.currentTimeMillis()))
    }
}