package com.example.modularapp.core.network

import  kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer

/**
 * The HTTP contract. Feature data sources call this and never import Ktor.
 *
 * It never throws for a failed request: every outcome, including no
 * connection and a body that does not decode, comes back as an [AppResult].
 * Coroutine cancellation is the one exception, and still propagates.
 */
interface NetworkClient {
    suspend fun <T> get(
        path: String,
        response: DeserializationStrategy<T>,
        query: Map<String, String> = emptyMap(),
    ): AppResult<T>

    suspend fun <B, T> post(
        path: String,
        body: B,
        request: SerializationStrategy<B>,
        response: DeserializationStrategy<T>,
    ): AppResult<T>
}

/** `client.get<List<PostDto>>("posts")` — the serializer inferred from the type. */
suspend inline fun <reified T> NetworkClient.get(
    path: String,
    query: Map<String, String> = emptyMap(),
): AppResult<T> = get(path, serializer<T>(), query)

suspend inline fun <reified B, reified T> NetworkClient.post(path: String, body: B): AppResult<T> =
    post(path, body, serializer<B>(), serializer<T>())
