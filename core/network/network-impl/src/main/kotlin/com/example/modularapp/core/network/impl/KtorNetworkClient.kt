package com.example.modularapp.core.network.impl

import com.example.modularapp.core.network.AppResult
import com.example.modularapp.core.network.NetworkClient
import com.example.modularapp.core.network.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * [NetworkClient] over Ktor. JSON is encoded and decoded here with the
 * strategies the caller passes, so no content-negotiation plugin is needed
 * and nothing reflective is involved.
 */
internal class KtorNetworkClient(
    private val client: HttpClient,
    private val json: Json = DefaultJson,
) : NetworkClient {

    override suspend fun <T> get(
        path: String,
        response: DeserializationStrategy<T>,
        query: Map<String, String>,
    ): AppResult<T> = execute(response) {
        client.get(path) {
            query.forEach { (key, value) -> parameter(key, value) }
        }
    }

    override suspend fun <B, T> post(
        path: String,
        body: B,
        request: SerializationStrategy<B>,
        response: DeserializationStrategy<T>,
    ): AppResult<T> = execute(response) {
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(request, body))
        }
    }

    @Suppress("TooGenericExceptionCaught") // Every failure becomes a value; see NetworkClient.
    private suspend fun <T> execute(
        response: DeserializationStrategy<T>,
        call: suspend () -> HttpResponse,
    ): AppResult<T> = try {
        AppResult.Success(json.decodeFromString(response, call().bodyAsText()))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        AppResult.Failure(error.toNetworkError())
    }

    private fun Throwable.toNetworkError(): NetworkError = when (this) {
        is ResponseException -> NetworkError.Http(response.status.value)
        is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> NetworkError.Timeout
        is SerializationException, is IllegalArgumentException -> NetworkError.Serialization(this)
        is IOException -> NetworkError.NoConnection
        else -> NetworkError.Unknown(this)
    }

    companion object {
        val DefaultJson = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
