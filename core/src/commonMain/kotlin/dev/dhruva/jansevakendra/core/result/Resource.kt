package dev.dhruva.jansevakendra.core.result

/**
 * Typed domain errors for production error handling.
 */
sealed class AppError(open val message: String, open val cause: Throwable? = null) {
    data class Network(override val message: String = "Network connection failed. Please check your internet.", override val cause: Throwable? = null) : AppError(message, cause)
    data class Unauthorized(override val message: String = "Session expired. Please log in again.", override val cause: Throwable? = null) : AppError(message, cause)
    data class Validation(override val message: String) : AppError(message)
    data class NotFound(override val message: String = "Requested service or resource not found.") : AppError(message)
    data class Server(override val message: String = "Server error occurred. Please try again later.", override val cause: Throwable? = null) : AppError(message, cause)
    data class Unknown(override val message: String = "An unexpected error occurred.", override val cause: Throwable? = null) : AppError(message, cause)

    fun toUserMessage(): String = message
}

/**
 * Result wrapper for async operations.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val error: AppError) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrDefault(default: @UnsafeVariance T): T = (this as? Success)?.data ?: default

    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError) -> Unit): Resource<T> {
        if (this is Error) action(error)
        return this
    }

    inline fun <R> map(transform: (T) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(error)
            is Loading -> Loading
        }
    }
}
