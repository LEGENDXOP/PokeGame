import TotalErrors.NetworkError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Response

fun main(){
    println("Hello, World!")
    val emailValidator = EmailValidatorImpl()
    val userValidation = UserValidation(emailValidator)
    println(userValidation.isValidEmail("hehe-bro@gmail.com"))
}

interface EmailValidator{
    fun matchEmail(email: String): Boolean
}
class hehe: EmailValidator{
    override fun matchEmail(email: String): Boolean {
        return email.contains(".com")
    }
}
class EmailValidatorImpl: EmailValidator{
    override fun matchEmail(email: String): Boolean {
        return email.contains("@")
    }

}
class UserValidation(
    private val emailValidator: EmailValidator
){
    fun isValidEmail(email: String): Boolean{
        return emailValidator.matchEmail(email.trim())
    }
}

interface Errors

sealed interface Result<out D, out E: Errors> {
    data class Success<out D> (val data: D): Result<D, Nothing>
    data class Error<out E: Errors> (val error: Errors): Result<Nothing, E>
}

suspend inline fun <reified Response> HttpClient.get(
    url: String,
    queryParameters: Map<String, Any?> = emptyMap()
): Result<Response, TotalErrors>{
    return safeCall {
        get {
            url(url)
            queryParameters.forEach{ (key, value) ->
                parameter(key, value)
            }
        }
    }
}

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, TotalErrors>{
    val response = try {
        execute()
    }catch (e: Exception){
        if (e is CancellationException) throw e
        return Result.Error(NetworkError.NO_INTERNET_CONNECTION)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, TotalErrors>{
    return when(response.status.value){
        in 200..299 -> Result.Success(response.body<T>())
        else -> Result.Error(NetworkError.SERVER_ERROR)
    }
}

fun<T, E: TotalErrors> Result<T, E>.asAnEmptyResult(): EmptyDataResult<E>{
    return map {  }
}

typealias EmptyDataResult<E> = Result<Unit, E>

fun <T, E: TotalErrors, R> Result<T, E>.map(transform: (T) -> R): Result<R, E>{
    return when(this){
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(error)
    }
}

sealed interface TotalErrors: Errors{
    enum class NetworkError: TotalErrors {
        NO_INTERNET_CONNECTION,
        SERVER_ERROR
    }
    enum class DatabaseError: TotalErrors {
        NO_DATA_FOUND,
        DATABASE_ERROR
    }
    enum class UnknownError: TotalErrors {
        UNKNOWN_ERROR
    }
}