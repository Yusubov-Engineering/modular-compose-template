package com.example.modularapp.core.network

/** The outcome of a network call. */
sealed interface AppResult<out T> {
    data class Success<out T>(val value: T) : AppResult<T>

    data class Failure(val error: NetworkError) : AppResult<Nothing>
}

/** Why a call failed. Exhaustive, so a new kind fails to compile rather than going unhandled. */
sealed interface NetworkError {
    /** No route to the server: offline, DNS, refused. */
    data object NoConnection : NetworkError

    data object Timeout : NetworkError

    /** The server answered with a non-2xx status. */
    data class Http(val code: Int) : NetworkError

    /** The body arrived but did not match the expected shape. */
    data class Serialization(val cause: Throwable) : NetworkError

    data class Unknown(val cause: Throwable) : NetworkError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.fold(onSuccess: (T) -> R, onFailure: (NetworkError) -> R): R = when (this) {
    is AppResult.Success -> onSuccess(value)
    is AppResult.Failure -> onFailure(error)
}
