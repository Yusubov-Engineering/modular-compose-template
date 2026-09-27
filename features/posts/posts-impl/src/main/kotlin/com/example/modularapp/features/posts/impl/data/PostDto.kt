package com.example.modularapp.features.posts.impl.data

import com.example.modularapp.features.posts.impl.domain.Post
import kotlinx.serialization.Serializable

/** The wire shape. Only the data layer sees it; the rest of the feature sees [Post]. */
@Serializable
internal data class PostDto(
    val id: Int,
    val userId: Int? = null,
    val title: String = "",
    val body: String = "",
) {
    fun toDomain(): Post = Post(id = id, title = title.trim(), body = body.trim())
}
