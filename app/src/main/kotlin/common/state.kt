package ui.common

import android.content.res.Resources
import com.example.photoalbum.R

sealed class State<out T, out E> {
    object      Loading                      : State<Nothing, Nothing>()
    data class  Success<out V>(val value: V) : State<V, Nothing>()
    data class  Failure<out E>(val error: E) : State<Nothing, E>()
}

sealed class Error {
    class  Api(val errorCode: Int, val errorMessage: String) : Error()
    object Timeout                                           : Error()
    object Other                                             : Error()
}

fun Error.getErrorMessage(resources: Resources, customText: String? = null): String =
    when (this) {
        is Error.Api        -> customText ?: resources.getString(R.string.something_went_wrong)
        is Error.Timeout    -> resources.getString(R.string.server_isnt_responding)
        is Error.Other      -> resources.getString(R.string.something_went_wrong)
    }