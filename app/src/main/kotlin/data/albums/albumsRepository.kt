package app.data.albums

import app.data.Api
import java.util.concurrent.TimeUnit

private val ALBUMS_CACHE_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(5)

interface AlbumsRepository {

    suspend fun getAlbums() : List<AlbumItemEntity>
}

class AlbumsRepositoryIml(private val albumsApi: Api, val albumsDao: AlbumsDao) : AlbumsRepository {

    override suspend fun getAlbums() : List<AlbumItemEntity> {
        var albums = albumsDao.getAll()

        if (albums.isNotEmpty()) {
            if ((albums.first().fetchTimestamp + ALBUMS_CACHE_EXPIRATION_TIME) < System.currentTimeMillis()) {
                albums = fetchAlbums()

                albumsDao.deleteAll()
                albumsDao.insertAll(albums)
            }
        } else {
            albums = fetchAlbums()
            albumsDao.insertAll(albums)
        }
        return albums
    }

    private suspend fun fetchAlbums() = albumsApi.getAlbums().map {
        it.copy(fetchTimestamp = System.currentTimeMillis())
    }

}