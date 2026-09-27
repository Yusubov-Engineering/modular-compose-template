package com.example.modularapp.features.posts.impl.data

import com.example.modularapp.core.network.AppResult
import com.example.modularapp.core.network.NetworkClient
import com.example.modularapp.core.network.NetworkError
import com.example.modularapp.core.network.get
import com.example.modularapp.features.posts.impl.domain.Post
import com.example.modularapp.features.posts.impl.domain.PostsFailure
import com.example.modularapp.features.posts.impl.domain.PostsRepository

/** DTO -> model, [NetworkError] -> [PostsFailure]. The only place a posts path is named. */
internal class PostsRepositoryImpl(private val client: NetworkClient) : PostsRepository {

    override suspend fun posts(): List<Post> =
        client.get<List<PostDto>>("posts").orThrow().map(PostDto::toDomain)

    override suspend fun post(id: Int): Post =
        client.get<PostDto>("posts/$id").orThrow().toDomain()

    private fun <T> AppResult<T>.orThrow(): T = when (this) {
        is AppResult.Success -> value
        is AppResult.Failure -> throw error.toFailure()
    }

    private fun NetworkError.toFailure(): PostsFailure = when (this) {
        NetworkError.NoConnection -> PostsFailure.NoConnection
        is NetworkError.Http -> if (code == HTTP_NOT_FOUND) PostsFailure.NotFound else PostsFailure.Unavailable
        NetworkError.Timeout, is NetworkError.Unknown -> PostsFailure.Unavailable
        is NetworkError.Serialization -> PostsFailure.Malformed
    }

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
