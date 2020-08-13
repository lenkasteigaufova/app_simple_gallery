package app.data.photos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ui.common.Error
import ui.common.State
import java.net.SocketTimeoutException

class PhotosViewModel(private val photosRepository: PhotosRepository) : ViewModel() {

    private val _photosLiveData : MutableLiveData<State<List<PhotoEntity>, Error>?> = MutableLiveData(null)
    val          photosLiveData : LiveData<State<List<PhotoEntity>, Error>?> get()  = _photosLiveData

    private val _photoUploadLiveData : MutableLiveData<State<Unit, Error>?> = MutableLiveData(null)
    val          photoUploadLiveData : LiveData<State<Unit, Error>?> get()  = _photoUploadLiveData

    fun fetchPhotos(albumId: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            _photosLiveData.postValue(State.Loading)
            val result = withContext(Dispatchers.IO) {
                photosRepository.runCatching {
                    getPhotos(albumId)
                }
            }
            val state = result.fold(
                onSuccess = { State.Success(it) },
                onFailure = {
                    when(it) {
                        is HttpException            -> State.Failure(Error.Api(it.code(), it.message()))
                        is SocketTimeoutException   -> State.Failure(Error.Timeout)
                        else                        -> State.Failure(Error.Other)
                    }
                }
            )
            _photosLiveData.postValue(state)
        }
    }

    fun uploadPhoto(photoTitle: String, albumId: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            _photoUploadLiveData.postValue(State.Loading)
            val result = withContext(Dispatchers.IO) {
                photosRepository.runCatching {
                    uploadPhoto(photoTitle, albumId)
                }
            }
            val state = result.fold(
                onSuccess = { State.Success(it) },
                onFailure = {
                    when (it) {
                        is HttpException            -> State.Failure(Error.Api(it.code(), it.message()))
                        is SocketTimeoutException   -> State.Failure(Error.Timeout)
                        else                        -> State.Failure(Error.Other)
                    }
                }
            )
            _photoUploadLiveData.postValue(state)
        }
    }
}