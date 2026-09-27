package com.example.modularapp.features.posts.impl

import com.example.modularapp.core.network.AppResult
import com.example.modularapp.core.network.NetworkClient
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

/** Answers every GET with [respond]'s JSON (or failure), decoded with the caller's strategy. */
internal class FakeNetworkClient(
    private val respond: (path: String) -> AppResult<String>,
) : NetworkClient {
    val requestedPaths = mutableListOf<String>()

    override suspend fun <T> get(
        path: String,
        response: DeserializationStrategy<T>,
        query: Map<String, String>,
    ): AppResult<T> {
        requestedPaths += path
        return when (val result = respond(path)) {
            is AppResult.Success -> AppResult.Success(Json.decodeFromString(response, result.value))
            is AppResult.Failure -> result
        }
    }

    override suspend fun <B, T> post(
        path: String,
        body: B,
        request: SerializationStrategy<B>,
        response: DeserializationStrategy<T>,
    ): AppResult<T> = error("Not used by the posts feature")
}
