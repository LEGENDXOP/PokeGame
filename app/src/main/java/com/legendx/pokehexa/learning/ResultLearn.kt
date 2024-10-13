package com.legendx.pokehexa.learning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GitHubData(
    val login: String,
    val id: Int,
    val bio: String,
    val name: String
)

fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                json = Json { ignoreUnknownKeys = true }
            )
        }
    }
    runBlocking {
        val url = "https://api.github.com/users/legendxop"
        val response = client.get<GitHubData>(url)
        when (response) {
            is Result.Success -> {
                println(response.data)
            }

            is Result.Error -> {
                println(response.error)
            }
        }
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
    route: String,
    body: Request
): Result<Response, NetworkError> {
    return safeCall {
        post {
            url(route)
            setBody(body)
        }
    }
}

suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = emptyMap()
): Result<Response, NetworkError> {
    return safeCall {
        get {
            url(route)
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, NetworkError> {
    val response = try {
        execute()
    } catch (e: Exception) {
        return Result.Error(NetworkError.NO_INTERNET_CONNECTION)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, NetworkError> {
    return when (response.status.value) {
        in 200..299 -> Result.Success(response.body<T>())
        in 400..499 -> Result.Error(NetworkError.NO_INTERNET_CONNECTION)
        in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
        else -> Result.Error(NetworkError.UNKNOWN_ERROR)
    }
}

@Composable
fun <T> ObserveAsEvents(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEvent: (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, key1, key2, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(onEvent)
        }
    }
}

interface Errors

sealed interface Result<out D, out E : Errors> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : Errors>(val error: E) : Result<Nothing, E>
}

fun <T, E : Errors> Result<T, E>.asAnEmptyDataResult(): Result<Unit, E> {
    return map { }
}

fun <T, E : Errors, R> Result<T, E>.map(mapping: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(mapping(data))
        is Result.Error -> Result.Error(error)
    }
}
typealias EmptyDataResult<E> = Result<Unit, E>


fun <D, E : Errors> Result<D, E>.onSuccess(block: (D) -> Unit): Result<D, E> {
    if (this is Result.Success) {
        block(this.data)
    }
    return this
}

enum class NetworkError : Errors {
    NO_INTERNET_CONNECTION,
    SERVER_ERROR,
    UNKNOWN_ERROR
}

enum class LocalError : Errors {
    FILE_NOT_FOUND,
    NO_STORAGE,
    NO_PERMISSION,
    UNKNOWN_ERROR
}

data class ExternalError(
    val message: String
) : Errors

fun validatePassword(password: String): Result<Int, Errors> {
    return if (password.length < 6) {
        Result.Error(LocalError.FILE_NOT_FOUND)
    } else {
        Result.Success(password.toInt())
    }
}