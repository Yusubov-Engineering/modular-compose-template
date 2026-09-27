package com.example.modularapp.features.posts.impl.domain

/**
 * The feature's data contract. Every function either returns or throws a
 * [PostsFailure] — nothing else escapes, so a caller catches exactly one
 * type and `when` over it is exhaustive.
 */
internal interface PostsRepository {
    suspend fun posts(): List<Post>

    suspend fun post(id: Int): Post
}
