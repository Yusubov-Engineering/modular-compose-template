package com.example.modularapp.features.posts.impl.router

import com.example.modularapp.core.router.AppRoute
import kotlinx.serialization.Serializable

// Internal: no other module can name, or build, a posts route. They get one
// from PostsLauncher, typed as AppRoute.

@Serializable
internal data object PostsRoute : AppRoute

@Serializable
internal data class PostDetailsRoute(val postId: Int) : AppRoute
