package app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.data.albums.AlbumItemEntity
import app.data.albums.AlbumsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ui.common.Error
import ui.common.State
import java.net.SocketTimeoutException

class AlbumsViewModel(private val albumsRepository: AlbumsRepository) : ViewModel() {

    private val _albumsLiveData : MutableLiveData<State<List<AlbumItemEntity>, Error>?> = MutableLiveData(null)
    val          albumsLiveData : LiveData<State<List<AlbumItemEntity>, Error>?> get()  = _albumsLiveData

    fun fetchAlbums() {
        viewModelScope.launch(Dispatchers.Main) {
            _albumsLiveData.postValue(State.Loading)
            val result = withContext(Dispatchers.IO) {
                albumsRepository.runCatching {
                    getAlbums()
                }
            }
            val state = result.fold(
                onSuccess = { State.Success(it) },
                onFailure = {
                    when(it){
                        is HttpException          -> State.Failure(Error.Api(it.code(), it.message()))
                        is SocketTimeoutException -> State.Failure(Error.Timeout)
                        else                      -> State.Failure(Error.Other)
                    }
                }
            )
            _albumsLiveData.postValue(state)
        }
    }
}